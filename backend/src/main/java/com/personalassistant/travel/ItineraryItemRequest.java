package com.personalassistant.travel;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemRequest {

    @Min(value = 1, message = "Day number must be at least 1")
    private int dayNumber;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private String description;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;

    @DecimalMin(value = "0.0", message = "Estimated cost must be positive")
    @Builder.Default
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    private String notes;
}
