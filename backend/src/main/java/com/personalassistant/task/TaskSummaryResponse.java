package com.personalassistant.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryResponse {
    private long totalTasks;
    private long todoCount;
    private long inProgressCount;
    private long completedCount;
    private long overdueCount;
    private long dueTodayCount;
    private List<TaskResponse> todayTasks;
    private List<TaskResponse> overdueTasks;
    private List<TaskResponse> highPriorityTasks;
    private List<TaskResponse> upcomingTasks;
}
