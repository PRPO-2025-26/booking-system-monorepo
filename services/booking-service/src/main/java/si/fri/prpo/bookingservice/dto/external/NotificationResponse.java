package si.fri.prpo.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String type;
    private String channel;
    private String recipient;
    private String subject;
    private String content;
    private String status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
