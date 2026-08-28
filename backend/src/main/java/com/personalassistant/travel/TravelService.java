package com.personalassistant.travel;

import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TripRepository tripRepository;
    private final UserService userService;

    @Transactional
    public TripResponse generateTripPlan(String userId, GenerateTripPlanRequest request) {
        User user = userService.getUserById(userId);

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().plusDays(7);
        LocalDate endDate = startDate.plusDays(Math.max(request.getDays() - 1, 0));

        BigDecimal budget = request.getBudget() != null && request.getBudget().compareTo(BigDecimal.ZERO) > 0
                ? request.getBudget()
                : BigDecimal.valueOf(5000L * request.getDays() * request.getTravelers());

        String tripName = String.format("%d-Day %s Adventure", request.getDays(), request.getDestination());

        Trip trip = Trip.builder()
                .user(user)
                .name(tripName)
                .destination(request.getDestination().trim())
                .startDate(startDate)
                .endDate(endDate)
                .budget(budget)
                .currency(user.getCurrency())
                .description(String.format("AI-generated %d-day itinerary for %d traveler(s) to %s.",
                        request.getDays(), request.getTravelers(), request.getDestination()))
                .status(TripStatus.PLANNED)
                .build();

        List<ItineraryItem> items = buildSampleItinerary(trip, request.getDestination(), request.getDays(), budget);
        trip.setItineraryItems(items);

        Trip saved = tripRepository.save(trip);
        return TripResponse.fromEntity(saved);
    }

    private List<ItineraryItem> buildSampleItinerary(Trip trip, String destination, int days, BigDecimal totalBudget) {
        List<ItineraryItem> items = new ArrayList<>();
        BigDecimal costPerDay = totalBudget.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
        BigDecimal morningCost = costPerDay.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal afternoonCost = costPerDay.multiply(BigDecimal.valueOf(0.4)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal eveningCost = costPerDay.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);

        String dest = destination.toLowerCase();

        for (int day = 1; day <= days; day++) {
            if (dest.contains("bangalore") || dest.contains("bengaluru")) {
                if (day == 1) {
                    items.add(createItem(trip, 1, "Cubbon Park & Morning Walk", "Stroll through lush bamboo groves and the historic state library.", "Cubbon Park, Central Bangalore", LocalTime.of(8, 30), LocalTime.of(11, 0), morningCost, "Wear walking shoes"));
                    items.add(createItem(trip, 1, "Traditional South Indian Lunch & Vidhana Soudha", "Enjoy famous dosa & filter coffee at iconic eatery and view Neo-Dravidian architecture.", "MG Road / Vidhana Soudha", LocalTime.of(12, 30), LocalTime.of(15, 30), afternoonCost, "Photography permitted outside"));
                    items.add(createItem(trip, 1, "Indiranagar Cafe & Craft Brewery Tour", "Explore famous microbreweries and vibrant evening cafes.", "100ft Road, Indiranagar", LocalTime.of(18, 0), LocalTime.of(21, 30), eveningCost, "Table reservation recommended"));
                } else if (day == 2) {
                    items.add(createItem(trip, 2, "Lalbagh Botanical Garden & Glass House", "Tour the 240-acre garden with centuries-old trees and Victorian glasshouse.", "Lalbagh, South Bangalore", LocalTime.of(9, 0), LocalTime.of(12, 0), morningCost, "Morning flower blooms"));
                    items.add(createItem(trip, 2, "Bangalore Palace Royal Tour", "Tudor-style royal estate with fortified towers and wood carvings.", "Vasanth Nagar", LocalTime.of(13, 30), LocalTime.of(16, 30), afternoonCost, "Audio guides available"));
                    items.add(createItem(trip, 2, "Koramangala Dinner & Live Music", "Dine at artisan restaurants with live acoustic performances.", "5th Block, Koramangala", LocalTime.of(19, 0), LocalTime.of(22, 0), eveningCost, "Great rooftop views"));
                } else {
                    items.add(createItem(trip, day, "Art & Heritage Walk / Nandi Hills Excursion", "Experience sunrise view or cultural heritage gallery at NGMA.", "High Grounds / Nandi Hills", LocalTime.of(8, 0), LocalTime.of(12, 0), morningCost, "Carry light jacket"));
                    items.add(createItem(trip, day, "Commercial Street Souvenir Shopping", "Explore vibrant silk and handicraft markets.", "Commercial Street, Tasker Town", LocalTime.of(14, 0), LocalTime.of(17, 0), afternoonCost, "Cash/UPI accepted widely"));
                    items.add(createItem(trip, day, "Farewell Dinner & Rooftop Lounge", "Celebrate with signature culinary delights overlooking city skyline.", "Lavelle Road", LocalTime.of(19, 30), LocalTime.of(22, 0), eveningCost, "Relaxed ambiance"));
                }
            } else {
                items.add(createItem(trip, day, String.format("Day %d: Morning City Exploration", day), "Discover top local landmarks, heritage sites, and famous breakfast spots.", destination + " City Center", LocalTime.of(9, 0), LocalTime.of(12, 0), morningCost, "Camera and comfortable shoes"));
                items.add(createItem(trip, day, String.format("Day %d: Cultural Experience & Cuisine", day), "Visit museums or nature parks and savor regional food specialities.", destination + " Cultural District", LocalTime.of(13, 0), LocalTime.of(16, 30), afternoonCost, "Try regional favorites"));
                items.add(createItem(trip, day, String.format("Day %d: Evening Sunset & Leisure", day), "Relax by picturesque viewpoints or bustling evening promenades.", destination + " Promenade", LocalTime.of(18, 0), LocalTime.of(21, 30), eveningCost, "Scenic sunset views"));
            }
        }
        return items;
    }

    private ItineraryItem createItem(
            Trip trip, int day, String title, String desc, String location,
            LocalTime start, LocalTime end, BigDecimal cost, String notes
    ) {
        return ItineraryItem.builder()
                .trip(trip)
                .dayNumber(day)
                .title(title)
                .description(desc)
                .location(location)
                .startTime(start)
                .endTime(end)
                .estimatedCost(cost)
                .notes(notes)
                .build();
    }
}
