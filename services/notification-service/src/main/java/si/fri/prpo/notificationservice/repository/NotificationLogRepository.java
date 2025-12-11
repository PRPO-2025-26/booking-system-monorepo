package si.fri.prpo.notificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.fri.prpo.notificationservice.entity.NotificationChannel;
import si.fri.prpo.notificationservice.entity.NotificationLog;
import si.fri.prpo.notificationservice.entity.NotificationStatus;
import si.fri.prpo.notificationservice.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserId(Long userId);

    List<NotificationLog> findByBookingId(Long bookingId);

    List<NotificationLog> findByPaymentId(Long paymentId);

    List<NotificationLog> findByEventId(Long eventId);

    List<NotificationLog> findByStatus(NotificationStatus status);

    List<NotificationLog> findByType(NotificationType type);

    List<NotificationLog> findByChannel(NotificationChannel channel);

    List<NotificationLog> findByUserIdAndStatus(Long userId, NotificationStatus status);

    List<NotificationLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<NotificationLog> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
}
