package si.fri.prpo.bookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.bookingservice.dto.BookingRequest;
import si.fri.prpo.bookingservice.dto.BookingResponse;
import si.fri.prpo.bookingservice.dto.UpdateBookingStatusRequest;
import si.fri.prpo.bookingservice.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Ustvari novo rezervacijo
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BookingRequest request) {
        log.info("Received create booking request from user {}", userId);
        try {
            BookingResponse response = bookingService.createBooking(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.error("Booking conflict: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Pridobi vse moje rezervacije
     * GET /api/bookings/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching all bookings for user {}", userId);
        List<BookingResponse> bookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Pridobi prihodnje rezervacije
     * GET /api/bookings/my/upcoming
     */
    @GetMapping("/my/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching upcoming bookings for user {}", userId);
        List<BookingResponse> bookings = bookingService.getUpcomingBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Pridobi pretekle rezervacije
     * GET /api/bookings/my/past
     */
    @GetMapping("/my/past")
    public ResponseEntity<List<BookingResponse>> getPastBookings(
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching past bookings for user {}", userId);
        List<BookingResponse> bookings = bookingService.getPastBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Pridobi specifično rezervacijo
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Fetching booking {} for user {}", id, userId);
        BookingResponse booking = bookingService.getBookingById(id, userId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Pridobi vse rezervacije za določen objekt
     * GET /api/bookings/facility/{facilityId}
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByFacility(
            @PathVariable Long facilityId) {
        log.info("Fetching bookings for facility {}", facilityId);
        List<BookingResponse> bookings = bookingService.getBookingsByFacility(facilityId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Posodobi status rezervacije
     * PATCH /api/bookings/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        log.info("Updating status of booking {} to {} by user {}", id, request.getStatus(), userId);
        BookingResponse booking = bookingService.updateBookingStatus(id, userId, request);
        return ResponseEntity.ok(booking);
    }

    /**
     * Prekliči rezervacijo
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        log.info("Cancelling booking {} by user {}", id, userId);
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.noContent().build();
    }
}
