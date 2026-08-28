package com.personalassistant.travel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryResponse {
    private long totalTrips;
    private long plannedTrips;
    private long activeTrips;
    private long completedTrips;
    private TripResponse nextUpcomingTrip;
    private List<TripResponse> recentTrips;
}
