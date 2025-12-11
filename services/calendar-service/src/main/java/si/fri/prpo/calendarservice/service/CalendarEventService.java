package si.fri.prpo.calendarservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.fri.prpo.calendarservice.dto.EventRequest;
import si.fri.prpo.calendarservice.dto.EventResponse;
import si.fri.prpo.calendarservice.entity.CalendarEvent;
import si.fri.prpo.calendarservice.entity.EventStatus;
import si.fri.prpo.calendarservice.entity.SyncStatus;
import si.fri.prpo.calendarservice.exception.CalendarException;
import si.fri.prpo.calendarservice.exception.EventNotFoundException;
import si.fri.prpo.calendarservice.repository.CalendarEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository eventRepository;
    private final GoogleCalendarService googleCalendarService;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        log.info("Creating calendar event for booking {}", request.getBookingId());

        // Check if event already exists for this booking
        if (eventRepository.existsByBookingId(request.getBookingId())) {
            throw new CalendarException("Calendar event already exists for booking " + request.getBookingId());
        }

        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new CalendarException("End time must be after start time");
        }

        // Create event entity
        CalendarEvent event = CalendarEvent.builder()
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .facilityId(request.getFacilityId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .status(EventStatus.SCHEDULED)
                .syncStatus(SyncStatus.PENDING)
                .build();

        // Try to sync with Google Calendar
        try {
            String googleEventId = googleCalendarService.createEvent(event);
            event.setGoogleEventId(googleEventId);
            event.setSyncStatus(googleCalendarService.isMockMode() ? SyncStatus.MOCK : SyncStatus.SYNCED);
            event.setLastSyncedAt(LocalDateTime.now());
            event.setGoogleEventLink("https://calendar.google.com/calendar/event?eid=" + googleEventId);
            log.info("Event synced with Google Calendar: {}", googleEventId);
        } catch (Exception e) {
            log.warn("Failed to sync with Google Calendar: {}", e.getMessage());
            event.setSyncStatus(SyncStatus.FAILED);
            event.setSyncErrorMessage(e.getMessage());
        }

        CalendarEvent savedEvent = eventRepository.save(event);
        log.info("Calendar event created with ID: {}", savedEvent.getId());

        return mapToResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventByBookingId(Long bookingId) {
        CalendarEvent event = eventRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new EventNotFoundException("Event not found for booking: " + bookingId));
        return mapToResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUserEvents(Long userId) {
        return eventRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getUpcomingEvents(Long userId) {
        return eventRepository.findByUserIdAndStartTimeAfter(userId, LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        // Update fields
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());

        // Try to sync with Google Calendar
        if (event.getGoogleEventId() != null) {
            try {
                googleCalendarService.updateEvent(event);
                event.setSyncStatus(googleCalendarService.isMockMode() ? SyncStatus.MOCK : SyncStatus.SYNCED);
                event.setLastSyncedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("Failed to sync update with Google Calendar: {}", e.getMessage());
                event.setSyncStatus(SyncStatus.FAILED);
                event.setSyncErrorMessage(e.getMessage());
            }
        }

        CalendarEvent updatedEvent = eventRepository.save(event);
        return mapToResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        // Delete from Google Calendar
        if (event.getGoogleEventId() != null) {
            try {
                googleCalendarService.deleteEvent(event.getGoogleEventId());
            } catch (Exception e) {
                log.warn("Failed to delete from Google Calendar: {}", e.getMessage());
            }
        }

        eventRepository.delete(event);
        log.info("Calendar event deleted: {}", id);
    }

    @Transactional
    public EventResponse cancelEvent(Long id) {
        CalendarEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));

        event.setStatus(EventStatus.CANCELLED);

        // Delete from Google Calendar
        if (event.getGoogleEventId() != null) {
            try {
                googleCalendarService.deleteEvent(event.getGoogleEventId());
                event.setSyncStatus(SyncStatus.SYNCED);
                event.setLastSyncedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.warn("Failed to delete from Google Calendar: {}", e.getMessage());
                event.setSyncStatus(SyncStatus.FAILED);
                event.setSyncErrorMessage(e.getMessage());
            }
        }

        CalendarEvent savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    private EventResponse mapToResponse(CalendarEvent event) {
        return EventResponse.builder()
                .id(event.getId())
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .facilityId(event.getFacilityId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .googleEventId(event.getGoogleEventId())
                .googleCalendarId(event.getGoogleCalendarId())
                .googleEventLink(event.getGoogleEventLink())
                .status(event.getStatus())
                .syncStatus(event.getSyncStatus())
                .lastSyncedAt(event.getLastSyncedAt())
                .syncErrorMessage(event.getSyncErrorMessage())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
