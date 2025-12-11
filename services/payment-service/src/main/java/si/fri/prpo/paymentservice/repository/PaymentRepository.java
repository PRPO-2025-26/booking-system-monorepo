package si.fri.prpo.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.fri.prpo.paymentservice.entity.Payment;
import si.fri.prpo.paymentservice.entity.Payment.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Najdi plačilo po booking ID
    Optional<Payment> findByBookingId(Long bookingId);

    // Najdi vsa plačila uporabnika
    List<Payment> findByUserId(Long userId);

    // Najdi plačila po statusu
    List<Payment> findByStatus(PaymentStatus status);

    // Najdi plačilo po Stripe Payment Intent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    // Najdi plačilo po Stripe Checkout Session ID
    Optional<Payment> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    // Najdi vsa uspešna plačila uporabnika
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED' ORDER BY p.completedAt DESC")
    List<Payment> findCompletedPaymentsByUserId(@Param("userId") Long userId);

    // Preveri, ali ima booking že plačilo
    boolean existsByBookingId(Long bookingId);
}
