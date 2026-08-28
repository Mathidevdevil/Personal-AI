package com.personalassistant.task;

import com.personalassistant.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    Page<Task> findByUserOrderByDueDateAscCreatedAtDesc(User user, Pageable pageable);

    Page<Task> findByUserAndStatusOrderByDueDateAscCreatedAtDesc(User user, TaskStatus status, Pageable pageable);

    Page<Task> findByUserAndPriorityOrderByDueDateAscCreatedAtDesc(User user, TaskPriority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> findWithFilters(
            @Param("user") User user,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND t.dueDate < :now ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("user") User user, @Param("now") Instant now);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND t.dueDate >= :start AND t.dueDate <= :end ORDER BY t.dueDate ASC")
    List<Task> findTasksDueBetween(@Param("user") User user, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND t.dueDate > :now ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("user") User user, @Param("now") Instant now);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND (t.priority = 'HIGH' OR t.priority = 'URGENT') ORDER BY t.dueDate ASC")
    List<Task> findHighPriorityTasks(@Param("user") User user);

    long countByUserAndStatus(User user, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND t.dueDate < :now")
    long countOverdueTasks(@Param("user") User user, @Param("now") Instant now);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.status != 'COMPLETED' AND t.status != 'CANCELLED' AND t.dueDate >= :start AND t.dueDate <= :end")
    long countTasksDueBetween(@Param("user") User user, @Param("start") Instant start, @Param("end") Instant end);
}
