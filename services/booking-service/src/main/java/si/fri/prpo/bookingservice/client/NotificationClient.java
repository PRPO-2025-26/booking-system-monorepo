package si.fri.prpo.bookingservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import si.fri.prpo.bookingservice.dto.external.NotificationRequest;
import si.fri.prpo.bookingservice.dto.external.NotificationResponse;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient webClient;

    public NotificationClient(@Qualifier("notificationWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Sending {} notification to {} via WebClient", request.getType(), request.getRecipient());
        log.debug("Request details: {}", request);

        try {
            NotificationResponse response = webClient.post()
                    .uri("") // Empty string - baseUrl already includes /api/notifications
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NotificationResponse.class)
                    .doOnError(error -> log.error("WebClient error: {}", error.getMessage(), error))
                    .block();

            log.info("Notification sent successfully: ID={}, Status={}",
                    response != null ? response.getId() : "null",
                    response != null ? response.getStatus() : "null");
            return response;
        } catch (Exception e) {
            log.error("CRITICAL: Error sending notification", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            // Don't throw - notification failure shouldn't break the flow
            return null;
        }
    }

    public void sendBookingConfirmation(Long userId, Long bookingId, String recipient, String facilityName,
            String startTime) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type("BOOKING_CONFIRMATION")
                .channel("EMAIL")
                .recipient(recipient)
                .subject("Potrditev rezervacije - " + facilityName)
                .content(String.format(
                        "Vaša rezervacija za %s je bila uspešno ustvarjena.\n\n" +
                                "Čas: %s\n\n" +
                                "Booking ID: %d",
                        facilityName, startTime, bookingId))
                .build();

        sendNotification(request);
    }

    public void sendPaymentConfirmation(Long userId, Long bookingId, Long paymentId, String recipient, String amount) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .paymentId(paymentId)
                .type("PAYMENT_CONFIRMATION")
                .channel("EMAIL")
                .recipient(recipient)
                .subject("Potrditev plačila")
                .content(String.format(
                        "Vaše plačilo v višini %s EUR je bilo uspešno potrjeno.\n\n" +
                                "Booking ID: %d\n" +
                                "Payment ID: %d",
                        amount, bookingId, paymentId))
                .build();

        sendNotification(request);
    }

    public void sendCalendarEventCreated(Long userId, Long bookingId, Long eventId, String recipient,
            String facilityName) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .eventId(eventId)
                .type("EVENT_REMINDER")
                .channel("EMAIL")
                .recipient(recipient)
                .subject("Dogodek dodan v Google Calendar")
                .content(String.format(
                        "Rezervacija za %s je bila dodana v vaš Google Calendar.\n\n" +
                                "Booking ID: %d\n" +
                                "Event ID: %d",
                        facilityName, bookingId, eventId))
                .build();

        sendNotification(request);
    }

    public void sendBookingCancellation(Long userId, Long bookingId, String recipient, String facilityName) {
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type("BOOKING_CANCELLATION")
                .channel("EMAIL")
                .recipient(recipient)
                .subject("Rezervacija preklicana - " + facilityName)
                .content(String.format(
                        "Vaša rezervacija za %s je bila preklicana.\n\n" +
                                "Booking ID: %d",
                        facilityName, bookingId))
                .build();

        sendNotification(request);
    }
}
