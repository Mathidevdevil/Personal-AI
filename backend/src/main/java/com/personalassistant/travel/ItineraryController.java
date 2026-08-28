package com.personalassistant.travel;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/itinerary")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItineraryItemResponse>>> getItinerary(@PathVariable String tripId) {
        String userId = SecurityUtils.getCurrentUserId();
        List<ItineraryItemResponse> response = itineraryService.getItinerary(userId, tripId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItineraryItemResponse>> addItineraryItem(
            @PathVariable String tripId,
            @Valid @RequestBody ItineraryItemRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        ItineraryItemResponse response = itineraryService.addItineraryItem(userId, tripId, request);
        return new ResponseEntity<>(ApiResponse.success("Itinerary item added successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItineraryItemResponse>> updateItineraryItem(
            @PathVariable String tripId,
            @PathVariable String itemId,
            @Valid @RequestBody ItineraryItemRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        ItineraryItemResponse response = itineraryService.updateItineraryItem(userId, tripId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Itinerary item updated successfully", response));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItineraryItem(
            @PathVariable String tripId,
            @PathVariable String itemId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        itineraryService.deleteItineraryItem(userId, tripId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Itinerary item deleted successfully", null));
    }
}
