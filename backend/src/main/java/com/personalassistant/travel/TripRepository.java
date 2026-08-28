package com.personalassistant.travel;

import com.personalassistant.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {

    Page<Trip> findByUserOrderByStartDateDesc(User user, Pageable pageable);

    List<Trip> findByUserOrderByStartDateAsc(User user);

    List<Trip> findByUserAndStatusOrderByStartDateAsc(User user, TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.user = :user AND t.status != 'CANCELLED' AND t.endDate >= :today ORDER BY t.startDate ASC")
    List<Trip> findUpcomingTrips(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.itineraryItems WHERE t.id = :id AND t.user = :user")
    Optional<Trip> findByIdWithItinerary(@Param("id") String id, @Param("user") User user);

    long countByUserAndStatus(User user, TripStatus status);
}
