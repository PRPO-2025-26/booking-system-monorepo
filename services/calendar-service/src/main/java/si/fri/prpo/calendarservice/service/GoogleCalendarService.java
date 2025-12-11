package si.fri.prpo.calendarservice.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import si.fri.prpo.calendarservice.entity.CalendarEvent;
import si.fri.prpo.calendarservice.exception.CalendarException;

import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class GoogleCalendarService {

    @Value("${google.calendar.mock-mode:true}")
    private boolean mockMode;

    @Value("${google.calendar.calendar-id:primary}")
    private String calendarId;

    private final Calendar calendarService;

    public GoogleCalendarService(@Value("${google.calendar.mock-mode:true}") boolean mockMode) {
        this.mockMode = mockMode;
        this.calendarService = null; // Will be initialized in real mode
        if (mockMode) {
            log.info("Google Calendar Service initialized in MOCK mode");
        }
    }

    /**
     * Create event in Google Calendar
     */
    public String createEvent(CalendarEvent calendarEvent) {
        if (mockMode) {
            return createMockEvent(calendarEvent);
        }

        try {
            Event event = buildGoogleEvent(calendarEvent);
            Event createdEvent = calendarService.events()
                    .insert(calendarId, event)
                    .execute();

            log.info("Created Google Calendar event: {}", createdEvent.getId());
            return createdEvent.getId();

        } catch (Exception e) {
            log.error("Failed to create Google Calendar event", e);
            throw new CalendarException("Failed to create Google Calendar event: " + e.getMessage(), e);
        }
    }

    /**
     * Update event in Google Calendar
     */
    public void updateEvent(CalendarEvent calendarEvent) {
        if (mockMode) {
            updateMockEvent(calendarEvent);
            return;
        }

        try {
            Event event = buildGoogleEvent(calendarEvent);
            calendarService.events()
                    .update(calendarId, calendarEvent.getGoogleEventId(), event)
                    .execute();

            log.info("Updated Google Calendar event: {}", calendarEvent.getGoogleEventId());

        } catch (Exception e) {
            log.error("Failed to update Google Calendar event", e);
            throw new CalendarException("Failed to update Google Calendar event: " + e.getMessage(), e);
        }
    }

    /**
     * Delete event from Google Calendar
     */
    public void deleteEvent(String googleEventId) {
        if (mockMode) {
            deleteMockEvent(googleEventId);
            return;
        }

        try {
            calendarService.events()
                    .delete(calendarId, googleEventId)
                    .execute();

            log.info("Deleted Google Calendar event: {}", googleEventId);

        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event", e);
            throw new CalendarException("Failed to delete Google Calendar event: " + e.getMessage(), e);
        }
    }

    /**
     * Build Google Event from CalendarEvent
     */
    private Event buildGoogleEvent(CalendarEvent calendarEvent) {
        Event event = new Event()
                .setSummary(calendarEvent.getTitle())
                .setDescription(calendarEvent.getDescription())
                .setLocation(calendarEvent.getLocation());

        // Set start time
        DateTime startDateTime = new DateTime(
                Date.from(calendarEvent.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Europe/Ljubljana");
        event.setStart(start);

        // Set end time
        DateTime endDateTime = new DateTime(
                Date.from(calendarEvent.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Europe/Ljubljana");
        event.setEnd(end);

        return event;
    }

    // ============================================
    // MOCK MODE METHODS
    // ============================================

    private String createMockEvent(CalendarEvent calendarEvent) {
        String mockId = "mock_event_" + System.currentTimeMillis();
        log.info("MOCK: Created calendar event with ID: {}", mockId);
        log.debug("MOCK Event details: {} - {} to {}",
                calendarEvent.getTitle(),
                calendarEvent.getStartTime(),
                calendarEvent.getEndTime());
        return mockId;
    }

    private void updateMockEvent(CalendarEvent calendarEvent) {
        log.info("MOCK: Updated calendar event: {}", calendarEvent.getGoogleEventId());
        log.debug("MOCK Event details: {} - {} to {}",
                calendarEvent.getTitle(),
                calendarEvent.getStartTime(),
                calendarEvent.getEndTime());
    }

    private void deleteMockEvent(String googleEventId) {
        log.info("MOCK: Deleted calendar event: {}", googleEventId);
    }

    public boolean isMockMode() {
        return mockMode;
    }
}
