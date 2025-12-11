package si.fri.prpo.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.fri.prpo.bookingservice.client.CalendarClient;
import si.fri.prpo.bookingservice.client.NotificationClient;
import si.fri.prpo.bookingservice.client.PaymentClient;
import si.fri.prpo.bookingservice.dto.BookingRequest;
import si.fri.prpo.bookingservice.dto.BookingResponse;
import si.fri.prpo.bookingservice.dto.UpdateBookingStatusRequest;
import si.fri.prpo.bookingservice.dto.external.CalendarEventRequest;
import si.fri.prpo.bookingservice.dto.external.CalendarEventResponse;
import si.fri.prpo.bookingservice.dto.external.PaymentCheckoutRequest;
import si.fri.prpo.bookingservice.dto.external.PaymentCheckoutResponse;
import si.fri.prpo.bookingservice.entity.Booking;
import si.fri.prpo.bookingservice.entity.Booking.BookingStatus;
import si.fri.prpo.bookingservice.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentClient paymentClient;
    private final CalendarClient calendarClient;
    private final NotificationClient notificationClient;

    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        log.info("Creating booking for user {} and facility {}", userId, request.getFacilityId());

        // 1. Validacija časa
        validateBookingTime(request.getStartTime(), request.getEndTime());

        // 2. Preveri, ali je objekt na voljo v tem času
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getFacilityId(),
                request.getStartTime(),
                request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Facility is not available at the selected time");
        }

        // 3. Izračunaj ceno (za zdaj fiksna 15€/uro - kasneje bo iz facility-service)
        BigDecimal totalPrice = calculatePrice(request.getStartTime(), request.getEndTime());

        // 4. Ustvari rezervacijo
        Booking booking = Booking.builder()
                .userId(userId)
                .facilityId(request.getFacilityId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice)
                .notes(request.getNotes())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {}", savedBooking.getId());

        // 5. Pošlji booking confirmation notification
        try {
            log.info("Attempting to send booking confirmation notification for booking {}", savedBooking.getId());
            notificationClient.sendBookingConfirmation(
                    userId,
                    savedBooking.getId(),
                    "user" + userId + "@example.com", // Mock email - kasneje iz Auth Service
                    "Facility #" + request.getFacilityId(), // Mock name - kasneje iz Facility Service
                    savedBooking.getStartTime().toString());
            log.info("Booking confirmation notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send booking confirmation notification", e);
            // Don't fail the booking if notification fails
        }

        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId) {
        log.info("Fetching all bookings for user {}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(Long userId) {
        log.info("Fetching upcoming bookings for user {}", userId);
        return bookingRepository.findUpcomingBookingsByUserId(userId, LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getPastBookings(Long userId) {
        log.info("Fetching past bookings for user {}", userId);
        return bookingRepository.findPastBookingsByUserId(userId, LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByFacility(Long facilityId) {
        log.info("Fetching all bookings for facility {}", facilityId);
        return bookingRepository.findByFacilityId(facilityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, Long userId) {
        log.info("Fetching booking {} for user {}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Preveri lastništvo (samo lastnik lahko vidi podrobnosti)
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to view this booking");
        }

        return mapToResponse(booking);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, Long userId, UpdateBookingStatusRequest request) {
        log.info("Updating booking {} status to {} by user {}", bookingId, request.getStatus(), userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Preveri lastništvo
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to update this booking");
        }

        // Validacija prehodov statusa
        validateStatusTransition(booking.getStatus(), request.getStatus());

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(request.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking {} status updated from {} to {}", bookingId, oldStatus, request.getStatus());

        // Handle status transitions with integrations
        if (request.getStatus() == BookingStatus.CONFIRMED && oldStatus == BookingStatus.PENDING) {
            handleBookingConfirmed(updatedBooking);
        }

        return mapToResponse(updatedBooking);
    }

    private void handleBookingConfirmed(Booking booking) {
        log.info("Handling booking confirmation for booking {}", booking.getId());

        // 1. Create payment checkout session
        try {
            PaymentCheckoutRequest paymentRequest = PaymentCheckoutRequest.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .amount(booking.getTotalPrice())
                    .currency("EUR")
                    .build();

            PaymentCheckoutResponse paymentResponse = paymentClient.createCheckoutSession(paymentRequest);
            log.info("Payment checkout session created: {}", paymentResponse.getSessionId());

            // 2. Create Google Calendar event (don't wait for payment)
            handleCalendarCreation(booking, paymentResponse.getId());

        } catch (Exception e) {
            log.error("Failed to create payment session for booking {}", booking.getId(), e);
            // Don't fail the booking if payment creation fails
        }
    }

    private void handleCalendarCreation(Booking booking, Long paymentId) {
        log.info("Creating calendar event for booking {}", booking.getId());

        try {
            // 1. Create Google Calendar event
            CalendarEventRequest calendarRequest = CalendarEventRequest.builder()
                    .bookingId(booking.getId())
                    .userId(booking.getUserId())
                    .facilityId(booking.getFacilityId())
                    .title("Booking #" + booking.getId() + " - Facility #" + booking.getFacilityId())
                    .location("Facility #" + booking.getFacilityId()) // Mock - later from Facility Service
                    .description("Reservation for facility. Total: " + booking.getTotalPrice() + " EUR")
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .build();

            CalendarEventResponse calendarResponse = calendarClient.createCalendarEvent(calendarRequest);
            log.info("Calendar event created: {}", calendarResponse.getId());

            // 2. Send payment confirmation notification
            log.info("Attempting to send payment confirmation notification");
            notificationClient.sendPaymentConfirmation(
                    booking.getUserId(),
                    booking.getId(),
                    paymentId,
                    "user" + booking.getUserId() + "@example.com",
                    booking.getTotalPrice().toString());
            log.info("Payment confirmation notification sent");

            // 3. Send calendar event created notification
            log.info("Attempting to send calendar event notification");
            notificationClient.sendCalendarEventCreated(
                    booking.getUserId(),
                    booking.getId(),
                    calendarResponse.getId(),
                    "user" + booking.getUserId() + "@example.com",
                    "Facility #" + booking.getFacilityId());
            log.info("Calendar event notification sent");

            log.info("All integrations completed successfully for booking {}", booking.getId());

        } catch (Exception e) {
            log.error("Failed to create calendar event or send notifications for booking {}", booking.getId(), e);
            // Don't fail the booking if calendar/notification fails
        }
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        log.info("Cancelling booking {} by user {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Preveri lastništvo
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to cancel this booking");
        }

        // Ne dovoli preklica že preklicanih ali zaključenih rezervacij
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        // Preveri, ali je rezervacija v prihodnosti (lahko prekličeš samo prihodnje)
        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", bookingId);

        // Send cancellation notification
        try {
            notificationClient.sendBookingCancellation(
                    userId,
                    bookingId,
                    "user" + userId + "@example.com",
                    "Facility #" + booking.getFacilityId());
        } catch (Exception e) {
            log.error("Failed to send booking cancellation notification", e);
        }
    }

    // Pomožne metode

    private void validateBookingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }

        // Minimalna rezervacija: 1 ura
        if (Duration.between(startTime, endTime).toHours() < 1) {
            throw new IllegalArgumentException("Minimum booking duration is 1 hour");
        }
    }

    private BigDecimal calculatePrice(LocalDateTime startTime, LocalDateTime endTime) {
        // Izračunaj ure
        long hours = Duration.between(startTime, endTime).toHours();

        // Fiksna cena 15€/uro (kasneje bo iz facility-service)
        BigDecimal pricePerHour = new BigDecimal("15.00");

        return pricePerHour.multiply(BigDecimal.valueOf(hours));
    }

    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        // PENDING -> CONFIRMED, CANCELLED
        // CONFIRMED -> CANCELLED, COMPLETED
        // CANCELLED -> ni več sprememb
        // COMPLETED -> ni več sprememb

        if (currentStatus == BookingStatus.CANCELLED || currentStatus == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change status of cancelled or completed booking");
        }

        if (currentStatus == BookingStatus.PENDING && newStatus == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot complete pending booking (must confirm first)");
        }
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .facilityId(booking.getFacilityId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
