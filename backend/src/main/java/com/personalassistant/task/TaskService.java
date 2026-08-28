package com.personalassistant.task;

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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    @Transactional
    public TaskResponse createTask(String userId, TaskRequest request) {
        User user = userService.getUserById(userId);

        Task task = Task.builder()
                .user(user)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .category(request.getCategory() != null ? request.getCategory().trim() : null)
                .completedAt(request.getStatus() == TaskStatus.COMPLETED ? Instant.now() : null)
                .build();

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(String userId, String taskId) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return TaskResponse.fromEntity(task);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasks(
            String userId, TaskStatus status, TaskPriority priority, String search, int page, int size
    ) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage = taskRepository.findWithFilters(user, status, priority, search, pageable);

        List<TaskResponse> content = taskPage.getContent().stream()
                .map(TaskResponse::fromEntity)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .content(content)
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .last(taskPage.isLast())
                .build();
    }

    @Transactional
    public TaskResponse updateTask(String userId, String taskId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        if (request.getStatus() != null) {
            if (request.getStatus() == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
            } else if (request.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        task.setDueDate(request.getDueDate());
        task.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public TaskResponse toggleComplete(String userId, String taskId) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
        } else {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(Instant.now());
        }

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public TaskResponse updateStatus(String userId, String taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        } else {
            task.setCompletedAt(null);
        }

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public TaskResponse updatePriority(String userId, String taskId, TaskPriority priority) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        task.setPriority(priority);
        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTask(String userId, String taskId) {
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(String userId) {
        User user = userService.getUserById(userId);
        ZoneId zoneId = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "UTC");
        LocalDate today = LocalDate.now(zoneId);
        Instant start = today.atStartOfDay(zoneId).toInstant();
        Instant end = today.atTime(23, 59, 59, 999999999).atZone(zoneId).toInstant();

        return taskRepository.findTasksDueBetween(user, start, end).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(String userId) {
        User user = userService.getUserById(userId);
        return taskRepository.findOverdueTasks(user, Instant.now()).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getUpcomingTasks(String userId) {
        User user = userService.getUserById(userId);
        return taskRepository.findUpcomingTasks(user, Instant.now()).stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskSummaryResponse getTaskSummary(String userId) {
        User user = userService.getUserById(userId);
        ZoneId zoneId = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "UTC");
        LocalDate today = LocalDate.now(zoneId);
        Instant start = today.atStartOfDay(zoneId).toInstant();
        Instant end = today.atTime(23, 59, 59, 999999999).atZone(zoneId).toInstant();
        Instant now = Instant.now();

        long todo = taskRepository.countByUserAndStatus(user, TaskStatus.TODO);
        long inProgress = taskRepository.countByUserAndStatus(user, TaskStatus.IN_PROGRESS);
        long completed = taskRepository.countByUserAndStatus(user, TaskStatus.COMPLETED);
        long overdue = taskRepository.countOverdueTasks(user, now);
        long dueToday = taskRepository.countTasksDueBetween(user, start, end);

        List<TaskResponse> todayList = taskRepository.findTasksDueBetween(user, start, end).stream()
                .map(TaskResponse::fromEntity)
                .toList();
        List<TaskResponse> overdueList = taskRepository.findOverdueTasks(user, now).stream()
                .map(TaskResponse::fromEntity)
                .toList();
        List<TaskResponse> highPriorityList = taskRepository.findHighPriorityTasks(user).stream()
                .map(TaskResponse::fromEntity)
                .toList();
        List<TaskResponse> upcomingList = taskRepository.findUpcomingTasks(user, now).stream()
                .map(TaskResponse::fromEntity)
                .toList();

        return TaskSummaryResponse.builder()
                .totalTasks(todo + inProgress + completed)
                .todoCount(todo)
                .inProgressCount(inProgress)
                .completedCount(completed)
                .overdueCount(overdue)
                .dueTodayCount(dueToday)
                .todayTasks(todayList)
                .overdueTasks(overdueList)
                .highPriorityTasks(highPriorityList)
                .upcomingTasks(upcomingList)
                .build();
    }
}
