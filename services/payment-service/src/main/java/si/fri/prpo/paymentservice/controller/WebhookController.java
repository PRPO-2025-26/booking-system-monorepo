package si.fri.prpo.paymentservice.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.paymentservice.entity.Payment.PaymentStatus;
import si.fri.prpo.paymentservice.service.PaymentService;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Stripe Webhook endpoint
     * POST /api/payments/webhook
     */
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook");

        Event event;

        try {
            // Verificiraj webhook podpis (varnostna preverjanje)
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Webhook event type: {}", event.getType());

        // Obdelaj različne tipe dogodkov
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;

            case "checkout.session.expired":
                handleCheckoutSessionExpired(event);
                break;

            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Obdelaj uspešno zaključen checkout
     */
    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            log.info("Checkout session completed: {}", session.getId());
            paymentService.updatePaymentStatus(
                    session.getId(),
                    PaymentStatus.COMPLETED,
                    session.getPaymentMethodTypes().get(0));
        }
    }

    /**
     * Obdelaj potečen checkout
     */
    private void handleCheckoutSessionExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session != null) {
            log.info("Checkout session expired: {}", session.getId());
            paymentService.markPaymentAsFailed(session.getId(), "Checkout session expired");
        }
    }

    /**
     * Obdelaj uspešen payment intent
     */
    private void handlePaymentIntentSucceeded(Event event) {
        log.info("Payment intent succeeded");
        // Dodatna obdelava po potrebi
    }

    /**
     * Obdelaj neuspešen payment intent
     */
    private void handlePaymentIntentFailed(Event event) {
        log.info("Payment intent failed");
        // Dodatna obdelava po potrebi
    }
}
