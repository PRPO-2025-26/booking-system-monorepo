package si.fri.prpo.paymentservice.service;

import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.fri.prpo.paymentservice.dto.PaymentRequest;
import si.fri.prpo.paymentservice.dto.PaymentResponse;
import si.fri.prpo.paymentservice.entity.Payment;
import si.fri.prpo.paymentservice.entity.Payment.PaymentStatus;
import si.fri.prpo.paymentservice.exception.PaymentException;
import si.fri.prpo.paymentservice.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;

    /**
     * Ustvari novo plačilo in Stripe Checkout Session
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for booking {}", request.getBookingId());

        // Preveri, ali booking že ima plačilo
        if (paymentRepository.existsByBookingId(request.getBookingId())) {
            throw new PaymentException("Payment already exists for booking " + request.getBookingId());
        }

        // MOCK MODE: Ustvari mock checkout session brez Stripe
        String mockSessionId = "cs_mock_" + System.currentTimeMillis();
        String mockPaymentIntentId = "pi_mock_" + System.currentTimeMillis();
        String mockCheckoutUrl = "http://localhost:3000/mock-checkout/" + mockSessionId;

        try {
            // Poskusi ustvariti pravi Stripe session
            Session session = stripeService.createCheckoutSession(
                    request.getBookingId(),
                    request.getAmount(),
                    request.getCurrency(),
                    request.getDescription());

            // Če uspe, uporabi prave podatke
            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .stripeCheckoutSessionId(session.getId())
                    .stripePaymentIntentId(session.getPaymentIntent())
                    .checkoutUrl(session.getUrl())
                    .description(request.getDescription())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {} (Stripe mode)", savedPayment.getId());
            return mapToResponse(savedPayment);

        } catch (Exception e) {
            // Če Stripe ne deluje, uporabi mock mode
            log.warn("Stripe API failed, using MOCK mode: {}", e.getMessage());

            Payment payment = Payment.builder()
                    .bookingId(request.getBookingId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.PENDING)
                    .stripeCheckoutSessionId(mockSessionId)
                    .stripePaymentIntentId(mockPaymentIntentId)
                    .checkoutUrl(mockCheckoutUrl)
                    .description(request.getDescription())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {} (MOCK mode)", savedPayment.getId());
            return mapToResponse(savedPayment);
        }
    }

    /**
     * Pridobi plačilo po ID-ju
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        return mapToResponse(payment);
    }

    /**
     * Pridobi plačilo po booking ID-ju
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentException("Payment not found for booking: " + bookingId));
        return mapToResponse(payment);
    }

    /**
     * Pridobi vsa plačila uporabnika
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pridobi uspešna plačila uporabnika
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCompletedPaymentsByUserId(Long userId) {
        return paymentRepository.findCompletedPaymentsByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Posodobi status plačila (iz webhook-a)
     */
    @Transactional
    public PaymentResponse updatePaymentStatus(String checkoutSessionId, PaymentStatus newStatus,
            String paymentMethod) {
        log.info("Updating payment status for session {} to {}", checkoutSessionId, newStatus);

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new PaymentException("Payment not found for session: " + checkoutSessionId));

        payment.setStatus(newStatus);
        payment.setPaymentMethod(paymentMethod);

        if (newStatus == PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment {} status updated to {}", updatedPayment.getId(), newStatus);

        return mapToResponse(updatedPayment);
    }

    /**
     * Označitev plačila kot neuspešno
     */
    @Transactional
    public PaymentResponse markPaymentAsFailed(String checkoutSessionId, String errorMessage) {
        log.info("Marking payment as failed for session {}", checkoutSessionId);

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new PaymentException("Payment not found for session: " + checkoutSessionId));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(errorMessage);

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }

    /**
     * Prekliči plačilo
     */
    @Transactional
    public void cancelPayment(Long paymentId) {
        log.info("Cancelling payment {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot cancel completed payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        log.info("Payment {} cancelled", paymentId);
    }

    /**
     * Map Payment entiteto na Response DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .checkoutUrl(payment.getCheckoutUrl())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeCheckoutSessionId(payment.getStripeCheckoutSessionId())
                .description(payment.getDescription())
                .paymentMethod(payment.getPaymentMethod())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }
}
