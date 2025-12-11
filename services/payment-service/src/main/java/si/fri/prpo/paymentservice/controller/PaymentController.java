package si.fri.prpo.paymentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.paymentservice.dto.PaymentRequest;
import si.fri.prpo.paymentservice.dto.PaymentResponse;
import si.fri.prpo.paymentservice.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Ustvari novo plačilo (checkout)
     * POST /api/payments/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating payment for booking {}", request.getBookingId());
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Pridobi plačilo po ID-ju
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Fetching payment {}", id);
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Pridobi plačilo po booking ID-ju
     * GET /api/payments/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBookingId(@PathVariable Long bookingId) {
        log.info("Fetching payment for booking {}", bookingId);
        PaymentResponse response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Pridobi status plačila
     * GET /api/payments/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long id) {
        log.info("Fetching payment status for {}", id);
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment.getStatus().toString());
    }

    /**
     * Pridobi vsa plačila uporabnika
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Long userId) {
        log.info("Fetching payments for user {}", userId);
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Pridobi uspešna plačila uporabnika
     * GET /api/payments/user/{userId}/completed
     */
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<PaymentResponse>> getCompletedPaymentsByUserId(@PathVariable Long userId) {
        log.info("Fetching completed payments for user {}", userId);
        List<PaymentResponse> payments = paymentService.getCompletedPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Prekliči plačilo
     * DELETE /api/payments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long id) {
        log.info("Cancelling payment {}", id);
        paymentService.cancelPayment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }
}
