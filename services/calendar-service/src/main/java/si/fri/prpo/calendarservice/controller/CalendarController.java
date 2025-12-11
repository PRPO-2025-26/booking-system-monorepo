package si.fri.prpo.calendarservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.calendarservice.dto.EventRequest;
import si.fri.prpo.calendarservice.dto.EventResponse;
import si.fri.prpo.calendarservice.service.CalendarEventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarEventService calendarEventService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Calendar Service is running");
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("Creating calendar event for booking {}", request.getBookingId());
        EventResponse response = calendarEventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        EventResponse response = calendarEventService.getEventById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/booking/{bookingId}")
    public ResponseEntity<EventResponse> getEventByBooking(@PathVariable Long bookingId) {
        EventResponse response = calendarEventService.getEventByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/user/{userId}")
    public ResponseEntity<List<EventResponse>> getUserEvents(@PathVariable Long userId) {
        List<EventResponse> events = calendarEventService.getUserEvents(userId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/user/{userId}/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents(@PathVariable Long userId) {
        List<EventResponse> events = calendarEventService.getUpcomingEvents(userId);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        EventResponse response = calendarEventService.updateEvent(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(@PathVariable Long id) {
        EventResponse response = calendarEventService.cancelEvent(id);
        return ResponseEntity.ok(response);
    }
}
