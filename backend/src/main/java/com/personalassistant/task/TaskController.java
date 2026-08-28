package com.personalassistant.task;

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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        PagedResponse<TaskResponse> response = taskService.getTasks(userId, status, priority, search, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.createTask(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Task created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.getTaskById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable String id,
            @Valid @RequestBody TaskRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.updateTask(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        taskService.deleteTask(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponse>> toggleComplete(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.toggleComplete(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable String id,
            @RequestParam TaskStatus status
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.updateStatus(userId, id, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<ApiResponse<TaskResponse>> updatePriority(
            @PathVariable String id,
            @RequestParam TaskPriority priority
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        TaskResponse response = taskService.updatePriority(userId, id, priority);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TaskSummaryResponse>> getTaskSummary() {
        String userId = SecurityUtils.getCurrentUserId();
        TaskSummaryResponse response = taskService.getTaskSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTodayTasks() {
        String userId = SecurityUtils.getCurrentUserId();
        List<TaskResponse> response = taskService.getTodayTasks(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks() {
        String userId = SecurityUtils.getCurrentUserId();
        List<TaskResponse> response = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getUpcomingTasks() {
        String userId = SecurityUtils.getCurrentUserId();
        List<TaskResponse> response = taskService.getUpcomingTasks(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
