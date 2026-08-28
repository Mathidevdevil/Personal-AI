package com.personalassistant.travel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTripPlanRequest {

    @NotBlank(message = "Destination cannot be empty")
    private String destination;

    @Min(value = 1, message = "Number of days must be at least 1")
    @Builder.Default
    private int days = 3;

    @Min(value = 1, message = "Number of travelers must be at least 1")
    @Builder.Default
    private int travelers = 1;

    @DecimalMin(value = "0.0", message = "Budget must be positive")
    private BigDecimal budget;

    private String preferences;

    private LocalDate startDate;
}
