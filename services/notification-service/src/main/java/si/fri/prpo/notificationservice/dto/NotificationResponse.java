package si.fri.prpo.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.fri.prpo.notificationservice.entity.NotificationChannel;
import si.fri.prpo.notificationservice.entity.NotificationStatus;
import si.fri.prpo.notificationservice.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private Long bookingId;
    private Long paymentId;
    private Long eventId;
    private NotificationType type;
    private NotificationChannel channel;
    private String recipient;
    private String subject;
    private String content;
    private NotificationStatus status;
    private LocalDateTime sentAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
