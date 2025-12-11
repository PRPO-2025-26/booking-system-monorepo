package si.fri.prpo.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private Long bookingId;
    private Long paymentId;
    private Long eventId;

    // Enums as Strings - will be parsed by Notification Service
    private String type; // NotificationType enum
    private String channel; // NotificationChannel enum

    private String recipient; // Email address
    private String subject; // Email subject
    private String content; // Email content
}
