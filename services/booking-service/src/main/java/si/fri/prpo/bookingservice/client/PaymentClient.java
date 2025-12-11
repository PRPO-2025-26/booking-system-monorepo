package si.fri.prpo.bookingservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import si.fri.prpo.bookingservice.dto.external.PaymentCheckoutRequest;
import si.fri.prpo.bookingservice.dto.external.PaymentCheckoutResponse;

@Slf4j
@Component
public class PaymentClient {

    private final WebClient webClient;

    public PaymentClient(@Qualifier("paymentWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public PaymentCheckoutResponse createCheckoutSession(PaymentCheckoutRequest request) {
        log.info("Creating payment checkout session for booking {}", request.getBookingId());

        try {
            return webClient.post()
                    .uri("/checkout")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentCheckoutResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating payment checkout session: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment session", e);
        }
    }

    public void completePaymentMock(String sessionId) {
        log.info("Completing mock payment for session {}", sessionId);

        try {
            webClient.post()
                    .uri("/mock/{sessionId}/complete", sessionId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.error("Error completing mock payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete mock payment", e);
        }
    }
}
