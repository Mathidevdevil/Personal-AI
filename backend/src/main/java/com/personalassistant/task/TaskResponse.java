package com.personalassistant.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Instant dueDate;
    private String category;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isOverdue;

    public static TaskResponse fromEntity(Task task) {
        boolean overdue = task.getDueDate() != null &&
                Instant.now().isAfter(task.getDueDate()) &&
                task.getStatus() != TaskStatus.COMPLETED &&
                task.getStatus() != TaskStatus.CANCELLED;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .category(task.getCategory())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isOverdue(overdue)
                .build();
    }
}
