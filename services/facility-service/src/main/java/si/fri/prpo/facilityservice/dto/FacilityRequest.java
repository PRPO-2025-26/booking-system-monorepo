package si.fri.prpo.facilityservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FacilityRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Type is required")
    private String type; // FOOTBALL_FIELD, BASKETBALL_COURT, TENNIS_COURT, GYM, SWIMMING_POOL

    @NotBlank(message = "Address is required")
    private String address;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerHour;

    private Boolean available = true;
}
