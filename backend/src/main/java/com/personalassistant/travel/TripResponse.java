package com.personalassistant.travel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private String id;
    private String name;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private String currency;
    private String description;
    private TripStatus status;
    private BigDecimal totalEstimatedCost;
    private BigDecimal remainingBudget;
    private int totalDays;
    private List<ItineraryItemResponse> itineraryItems;
    private Instant createdAt;
    private Instant updatedAt;

    public static TripResponse fromEntity(Trip trip) {
        BigDecimal totalCost = trip.getItineraryItems() != null
                ? trip.getItineraryItems().stream()
                .map(ItineraryItem::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;

        BigDecimal remaining = trip.getBudget().subtract(totalCost);

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;

        List<ItineraryItemResponse> items = trip.getItineraryItems() != null
                ? trip.getItineraryItems().stream().map(ItineraryItemResponse::fromEntity).toList()
                : List.of();

        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .currency(trip.getCurrency())
                .description(trip.getDescription())
                .status(trip.getStatus())
                .totalEstimatedCost(totalCost)
                .remainingBudget(remaining)
                .totalDays(Math.max(days, 1))
                .itineraryItems(items)
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }
}
