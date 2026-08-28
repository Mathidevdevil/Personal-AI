package com.personalassistant.ai;

import com.personalassistant.finance.FinanceDashboardService;
import com.personalassistant.finance.FinanceSummaryResponse;
import com.personalassistant.task.TaskService;
import com.personalassistant.task.TaskSummaryResponse;
import com.personalassistant.travel.TripService;
import com.personalassistant.travel.TripSummaryResponse;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AIContextService {

    private final UserService userService;
    private final FinanceDashboardService financeDashboardService;
    private final TaskService taskService;
    private final TripService tripService;

    public String buildSystemContext(String userId) {
        User user = userService.getUserById(userId);
        ZoneId zone = ZoneId.of(user.getTimezone() != null ? user.getTimezone() : "UTC");
        LocalDate today = LocalDate.now(zone);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You are the Personal AI Assistant for %s.\n", user.getName()));
        sb.append(String.format("User Preferred Currency: %s | Timezone: %s | Today's Date: %s\n\n",
                user.getCurrency(), user.getTimezone(), today.format(DateTimeFormatter.ISO_LOCAL_DATE)));

        // Finance context
        try {
            FinanceSummaryResponse fin = financeDashboardService.getSummary(userId, today.getYear(), today.getMonthValue());
            sb.append("--- Current Financial Context ---\n");
            sb.append(String.format("- Total Balance: %s %s\n", user.getCurrency(), fin.getCurrentBalance()));
            sb.append(String.format("- This Month Spending: %s %s | Month Income: %s %s\n",
                    user.getCurrency(), fin.getMonthlyExpenses(), user.getCurrency(), fin.getMonthlyIncome()));
            sb.append(String.format("- Budget: Set %s %s, Spent %s %s (%s%%)\n\n",
                    user.getCurrency(), fin.getTotalBudget(), user.getCurrency(), fin.getTotalBudgetSpent(),
                    String.format("%.1f", fin.getOverallBudgetPercentage())));
        } catch (Exception ignored) {}

        // Task context
        try {
            TaskSummaryResponse tasks = taskService.getTaskSummary(userId);
            sb.append("--- Current Tasks Context ---\n");
            sb.append(String.format("- Total Pending: %d (Todo: %d, In Progress: %d, Completed: %d)\n",
                    tasks.getTodoCount() + tasks.getInProgressCount(), tasks.getTodoCount(), tasks.getInProgressCount(), tasks.getCompletedCount()));
            sb.append(String.format("- Overdue Tasks Count: %d | Due Today: %d\n", tasks.getOverdueCount(), tasks.getDueTodayCount()));
            if (!tasks.getOverdueTasks().isEmpty()) {
                sb.append("  Overdue tasks: ");
                tasks.getOverdueTasks().stream().limit(3).forEach(t -> sb.append(String.format("'%s' (due %s), ", t.getTitle(), t.getDueDate())));
                sb.append("\n");
            }
            sb.append("\n");
        } catch (Exception ignored) {}

        // Travel context
        try {
            TripSummaryResponse trips = tripService.getTripSummary(userId);
            sb.append("--- Travel Context ---\n");
            if (trips.getNextUpcomingTrip() != null) {
                sb.append(String.format("- Next Upcoming Trip: %s to %s (from %s to %s, Budget: %s %s)\n",
                        trips.getNextUpcomingTrip().getName(),
                        trips.getNextUpcomingTrip().getDestination(),
                        trips.getNextUpcomingTrip().getStartDate(),
                        trips.getNextUpcomingTrip().getEndDate(),
                        trips.getNextUpcomingTrip().getCurrency(),
                        trips.getNextUpcomingTrip().getBudget()));
            } else {
                sb.append("- No upcoming trips scheduled.\n");
            }
            sb.append("\n");
        } catch (Exception ignored) {}

        sb.append("Always use the provided backend tools to perform modifications or fetch precise live data. ");
        sb.append("Format currency using the user's currency symbol (e.g. ₹ for INR). Never hallucinate successful actions.");

        return sb.toString();
    }
}
