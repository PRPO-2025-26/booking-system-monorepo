package si.fri.prpo.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCheckoutResponse {
    private Long id;
    private String sessionId;
    private String checkoutUrl;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
