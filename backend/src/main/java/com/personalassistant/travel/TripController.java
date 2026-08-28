package com.personalassistant.travel;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TravelService travelService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TripResponse>>> getTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        PagedResponse<TripResponse> response = tripService.getTrips(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@Valid @RequestBody TripRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        TripResponse response = tripService.createTrip(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Trip created successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TripResponse>> generateTripPlan(@Valid @RequestBody GenerateTripPlanRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        TripResponse response = travelService.generateTripPlan(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Trip plan generated successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        TripResponse response = tripService.getTripById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(
            @PathVariable String id,
            @Valid @RequestBody TripRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        TripResponse response = tripService.updateTrip(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Trip updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        tripService.deleteTrip(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully", null));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getUpcomingTrips() {
        String userId = SecurityUtils.getCurrentUserId();
        List<TripResponse> response = tripService.getUpcomingTrips(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TripSummaryResponse>> getTripSummary() {
        String userId = SecurityUtils.getCurrentUserId();
        TripSummaryResponse response = tripService.getTripSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
