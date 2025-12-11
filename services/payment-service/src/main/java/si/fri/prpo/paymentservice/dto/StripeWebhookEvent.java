package si.fri.prpo.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeWebhookEvent {
    private String eventType;
    private String paymentIntentId;
    private String checkoutSessionId;
    private String status;
    private String errorMessage;
}
