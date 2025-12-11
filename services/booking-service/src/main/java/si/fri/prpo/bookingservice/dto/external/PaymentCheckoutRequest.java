package si.fri.prpo.bookingservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCheckoutRequest {
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
}
