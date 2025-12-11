package si.fri.prpo.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import si.fri.prpo.paymentservice.exception.PaymentException;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    /**
     * Ustvari Stripe Checkout Session
     */
    public Session createCheckoutSession(Long bookingId, BigDecimal amount, String currency, String description) {
        try {
            log.info("Creating Stripe Checkout Session for booking {} with amount {} {}",
                    bookingId, amount, currency);

            // Stripe zahteva ceno v najmanjši enoti (cent za EUR)
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency.toLowerCase())
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Booking #" + bookingId)
                                                                    .setDescription(description != null ? description
                                                                            : "Sports Facility Booking")
                                                                    .build())
                                                    .build())
                                    .setQuantity(1L)
                                    .build())
                    .putMetadata("bookingId", bookingId.toString())
                    .build();

            Session session = Session.create(params);
            log.info("Stripe Checkout Session created: {}", session.getId());
            return session;

        } catch (StripeException e) {
            log.error("Stripe API error: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create Stripe checkout session: " + e.getMessage());
        }
    }

    /**
     * Pridobi Checkout Session po ID-ju
     */
    public Session retrieveCheckoutSession(String sessionId) {
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe session {}: {}", sessionId, e.getMessage());
            throw new PaymentException("Failed to retrieve checkout session: " + e.getMessage());
        }
    }
}
