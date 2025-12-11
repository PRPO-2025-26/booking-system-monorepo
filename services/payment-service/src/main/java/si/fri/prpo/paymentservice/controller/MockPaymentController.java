package si.fri.prpo.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.fri.prpo.paymentservice.entity.Payment.PaymentStatus;
import si.fri.prpo.paymentservice.service.PaymentService;

/**
 * Mock controller za simulacijo Stripe plačil v test okolju
 */
@RestController
@RequestMapping("/api/payments/mock")
@RequiredArgsConstructor
@Slf4j
public class MockPaymentController {

    private final PaymentService paymentService;

    /**
     * Simulira uspešno plačilo (za testiranje brez Stripe)
     * POST /api/payments/mock/{sessionId}/complete
     */
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<String> completePayment(@PathVariable String sessionId) {
        log.info("Mock: Completing payment for session {}", sessionId);

        try {
            paymentService.updatePaymentStatus(sessionId, PaymentStatus.COMPLETED, "mock_card");
            return ResponseEntity.ok("Payment completed successfully (MOCK)");
        } catch (Exception e) {
            log.error("Failed to complete mock payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    /**
     * Simulira neuspešno plačilo (za testiranje)
     * POST /api/payments/mock/{sessionId}/fail
     */
    @PostMapping("/{sessionId}/fail")
    public ResponseEntity<String> failPayment(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "Mock payment failed") String reason) {
        log.info("Mock: Failing payment for session {}", sessionId);

        try {
            paymentService.markPaymentAsFailed(sessionId, reason);
            return ResponseEntity.ok("Payment failed (MOCK)");
        } catch (Exception e) {
            log.error("Failed to fail mock payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    /**
     * Info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok(
                "Mock Payment Controller - For testing without real Stripe integration\n" +
                        "Endpoints:\n" +
                        "  POST /api/payments/mock/{sessionId}/complete - Complete payment\n" +
                        "  POST /api/payments/mock/{sessionId}/fail - Fail payment");
    }
}
