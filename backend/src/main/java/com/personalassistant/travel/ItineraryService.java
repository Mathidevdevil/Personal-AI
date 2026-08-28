package com.personalassistant.travel;

import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final TripRepository tripRepository;
    private final ItineraryItemRepository itineraryItemRepository;
    private final UserService userService;

    @Transactional
    public ItineraryItemResponse addItineraryItem(String userId, String tripId, ItineraryItemRequest request) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findById(tripId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        ItineraryItem item = ItineraryItem.builder()
                .trip(trip)
                .dayNumber(request.getDayNumber())
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .estimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : java.math.BigDecimal.ZERO)
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();

        ItineraryItem saved = itineraryItemRepository.save(item);
        return ItineraryItemResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ItineraryItemResponse> getItinerary(String userId, String tripId) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findById(tripId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        return itineraryItemRepository.findByTripOrderByDayNumberAscStartTimeAsc(trip).stream()
                .map(ItineraryItemResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ItineraryItemResponse updateItineraryItem(
            String userId, String tripId, String itemId, ItineraryItemRequest request
    ) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findById(tripId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        ItineraryItem item = itineraryItemRepository.findById(itemId)
                .filter(i -> i.getTrip().getId().equals(trip.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", "id", itemId));

        item.setDayNumber(request.getDayNumber());
        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        item.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        if (request.getEstimatedCost() != null) {
            item.setEstimatedCost(request.getEstimatedCost());
        }
        item.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        ItineraryItem updated = itineraryItemRepository.save(item);
        return ItineraryItemResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteItineraryItem(String userId, String tripId, String itemId) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findById(tripId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        ItineraryItem item = itineraryItemRepository.findById(itemId)
                .filter(i -> i.getTrip().getId().equals(trip.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryItem", "id", itemId));

        itineraryItemRepository.delete(item);
    }
}
