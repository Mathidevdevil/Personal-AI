package com.personalassistant.travel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TripRequest {

    @NotBlank(message = "Trip name cannot be empty")
    @Size(max = 150, message = "Trip name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Destination cannot be empty")
    @Size(max = 150, message = "Destination cannot exceed 150 characters")
    private String destination;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @DecimalMin(value = "0.0", message = "Budget must be positive")
    @Builder.Default
    private BigDecimal budget = BigDecimal.ZERO;

    @Builder.Default
    private String currency = "INR";

    private String description;

    @Builder.Default
    private TripStatus status = TripStatus.PLANNED;
}
