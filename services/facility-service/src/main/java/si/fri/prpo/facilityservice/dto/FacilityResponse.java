package si.fri.prpo.facilityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityResponse {
    private Long id;
    private String name;
    private String type;
    private String address;
    private String description;
    private Integer capacity;
    private BigDecimal pricePerHour;
    private Long ownerId;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
