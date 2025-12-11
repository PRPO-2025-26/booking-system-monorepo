package si.fri.prpo.bookingservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import si.fri.prpo.bookingservice.dto.external.CalendarEventRequest;
import si.fri.prpo.bookingservice.dto.external.CalendarEventResponse;

@Slf4j
@Component
public class CalendarClient {

    private final WebClient webClient;

    public CalendarClient(@Qualifier("calendarWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public CalendarEventResponse createCalendarEvent(CalendarEventRequest request) {
        log.info("Creating calendar event for booking {}", request.getBookingId());

        try {
            return webClient.post()
                    .uri("/events")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(CalendarEventResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create calendar event", e);
        }
    }

    public void cancelCalendarEvent(Long eventId) {
        log.info("Cancelling calendar event {}", eventId);

        try {
            webClient.post()
                    .uri("/events/{id}/cancel", eventId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error cancelling calendar event: {}", e.getMessage(), e);
            // Don't throw - calendar cancellation is not critical
            log.warn("Calendar event cancellation failed, continuing...");
        }
    }
}
