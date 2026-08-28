package com.personalassistant.travel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemResponse {
    private String id;
    private int dayNumber;
    private String title;
    private String description;
    private String location;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal estimatedCost;
    private String notes;
    private Instant createdAt;

    public static ItineraryItemResponse fromEntity(ItineraryItem item) {
        return ItineraryItemResponse.builder()
                .id(item.getId())
                .dayNumber(item.getDayNumber())
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .estimatedCost(item.getEstimatedCost())
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
