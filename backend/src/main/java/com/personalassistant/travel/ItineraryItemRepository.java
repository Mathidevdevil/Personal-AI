package com.personalassistant.travel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, String> {

    List<ItineraryItem> findByTripOrderByDayNumberAscStartTimeAsc(Trip trip);

    List<ItineraryItem> findByTripAndDayNumberOrderByStartTimeAsc(Trip trip, int dayNumber);
}
