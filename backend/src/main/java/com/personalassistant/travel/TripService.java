package com.personalassistant.travel;

import com.personalassistant.common.BadRequestException;
import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserService userService;

    @Transactional
    public TripResponse createTrip(String userId, TripRequest request) {
        User user = userService.getUserById(userId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Trip end date cannot be before start date");
        }

        Trip trip = Trip.builder()
                .user(user)
                .name(request.getName().trim())
                .destination(request.getDestination().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget() != null ? request.getBudget() : java.math.BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : user.getCurrency())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : TripStatus.PLANNED)
                .build();

        Trip saved = tripRepository.save(trip);
        return TripResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(String userId, String tripId) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findByIdWithItinerary(tripId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
        return TripResponse.fromEntity(trip);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TripResponse> getTrips(String userId, int page, int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Trip> tripPage = tripRepository.findByUserOrderByStartDateDesc(user, pageable);

        List<TripResponse> content = tripPage.getContent().stream()
                .map(TripResponse::fromEntity)
                .toList();

        return PagedResponse.<TripResponse>builder()
                .content(content)
                .page(tripPage.getNumber())
                .size(tripPage.getSize())
                .totalElements(tripPage.getTotalElements())
                .totalPages(tripPage.getTotalPages())
                .last(tripPage.isLast())
                .build();
    }

    @Transactional
    public TripResponse updateTrip(String userId, String tripId, TripRequest request) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findByIdWithItinerary(tripId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Trip end date cannot be before start date");
        }

        trip.setName(request.getName().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        if (request.getBudget() != null) {
            trip.setBudget(request.getBudget());
        }
        if (request.getCurrency() != null) {
            trip.setCurrency(request.getCurrency());
        }
        trip.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
        }

        Trip updated = tripRepository.save(trip);
        return TripResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTrip(String userId, String tripId) {
        User user = userService.getUserById(userId);
        Trip trip = tripRepository.findById(tripId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));
        tripRepository.delete(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getUpcomingTrips(String userId) {
        User user = userService.getUserById(userId);
        return tripRepository.findUpcomingTrips(user, LocalDate.now()).stream()
                .map(TripResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripSummaryResponse getTripSummary(String userId) {
        User user = userService.getUserById(userId);
        long total = tripRepository.count();
        long planned = tripRepository.countByUserAndStatus(user, TripStatus.PLANNED);
        long active = tripRepository.countByUserAndStatus(user, TripStatus.ACTIVE);
        long completed = tripRepository.countByUserAndStatus(user, TripStatus.COMPLETED);

        List<Trip> upcoming = tripRepository.findUpcomingTrips(user, LocalDate.now());
        TripResponse nextTrip = upcoming.isEmpty() ? null : TripResponse.fromEntity(upcoming.get(0));

        List<TripResponse> recent = tripRepository.findByUserOrderByStartDateDesc(user, PageRequest.of(0, 5))
                .getContent().stream()
                .map(TripResponse::fromEntity)
                .toList();

        return TripSummaryResponse.builder()
                .totalTrips(total)
                .plannedTrips(planned)
                .activeTrips(active)
                .completedTrips(completed)
                .nextUpcomingTrip(nextTrip)
                .recentTrips(recent)
                .build();
    }
}
