package si.fri.prpo.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.fri.prpo.bookingservice.dto.BookingRequest;
import si.fri.prpo.bookingservice.dto.BookingResponse;
import si.fri.prpo.bookingservice.dto.UpdateBookingStatusRequest;
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

        booking.setStatus(request.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking {} status updated to {}", bookingId, request.getStatus());
        return mapToResponse(updatedBooking);
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
