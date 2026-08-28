package com.personalassistant.ai;

import com.personalassistant.finance.*;
import com.personalassistant.task.*;
import com.personalassistant.travel.*;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIToolDispatcher {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BudgetService budgetService;
    private final FinanceDashboardService financeDashboardService;
    private final TaskService taskService;
    private final TripService tripService;
    private final TravelService travelService;
    private final UserService userService;

    public ToolResult executeTool(String userId, String toolName, Map<String, Object> args) {
        log.info("Executing AI tool '{}' for user '{}' with args: {}", toolName, userId, args);
        User user = userService.getUserById(userId);
        String currency = user.getCurrency();

        try {
            switch (toolName) {
                // --- Finance Tools ---
                case "createExpense": {
                    BigDecimal amount = parseBigDecimal(args.get("amount"));
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        return ToolResult.error(toolName, "finance", "Invalid expense amount. Must be greater than 0.");
                    }
                    String categoryStr = (String) args.getOrDefault("category", "OTHER");
                    ExpenseCategory category = ExpenseCategory.fromString(categoryStr);
                    String description = (String) args.getOrDefault("description", "Expense");
                    String paymentStr = (String) args.getOrDefault("paymentMethod", "UPI");
                    PaymentMethod paymentMethod = PaymentMethod.fromString(paymentStr);

                    ExpenseRequest req = ExpenseRequest.builder()
                            .amount(amount)
                            .category(category)
                            .description(description)
                            .paymentMethod(paymentMethod)
                            .transactionDate(Instant.now())
                            .build();

                    ExpenseResponse res = expenseService.createExpense(userId, req);
                    String msg = String.format("Added %s %s for %s (%s via %s).",
                            currency, res.getAmount(), res.getDescription(), res.getCategory(), res.getPaymentMethod());
                    return ToolResult.success(toolName, "finance", msg, res);
                }

                case "createIncome": {
                    BigDecimal amount = parseBigDecimal(args.get("amount"));
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        return ToolResult.error(toolName, "finance", "Invalid income amount.");
                    }
                    String source = (String) args.getOrDefault("source", "Other Income");
                    String description = (String) args.get("description");

                    IncomeRequest req = IncomeRequest.builder()
                            .amount(amount)
                            .source(source)
                            .description(description)
                            .incomeDate(Instant.now())
                            .build();

                    IncomeResponse res = incomeService.createIncome(userId, req);
                    String msg = String.format("Recorded income of %s %s from %s.", currency, res.getAmount(), res.getSource());
                    return ToolResult.success(toolName, "finance", msg, res);
                }

                case "getExpenses": {
                    String categoryStr = (String) args.get("category");
                    ExpenseCategory cat = categoryStr != null ? ExpenseCategory.fromString(categoryStr) : null;
                    var res = expenseService.getExpenses(userId, cat, null, null, 0, 5);
                    String msg = String.format("Found %d recent expenses.", res.getTotalElements());
                    return ToolResult.success(toolName, "finance", msg, res.getContent());
                }

                case "getMonthlySpending": {
                    LocalDate now = LocalDate.now();
                    int month = args.get("month") != null ? ((Number) args.get("month")).intValue() : now.getMonthValue();
                    int year = args.get("year") != null ? ((Number) args.get("year")).intValue() : now.getYear();

                    BigDecimal total = expenseService.getMonthlySpending(userId, year, month);
                    String msg = String.format("You have spent %s %s in %s %d.", currency, total != null ? total : "0", now.getMonth().name(), year);
                    return ToolResult.success(toolName, "finance", msg, Map.of("month", month, "year", year, "totalSpent", total, "currency", currency));
                }

                case "getBudgetStatus": {
                    String catStr = (String) args.get("category");
                    LocalDate now = LocalDate.now();
                    if (catStr != null && !catStr.isBlank()) {
                        ExpenseCategory cat = ExpenseCategory.fromString(catStr);
                        BudgetResponse b = budgetService.getBudgetForCategory(userId, cat, now.getMonthValue(), now.getYear());
                        String msg = String.format("%s budget: %s %s | Spent: %s %s | Remaining: %s %s (%.1f%% used).",
                                cat, currency, b.getAmount(), currency, b.getSpentAmount(), currency, b.getRemainingAmount(), b.getPercentageUsed());
                        return ToolResult.success(toolName, "finance", msg, b);
                    } else {
                        FinanceSummaryResponse summary = financeDashboardService.getSummary(userId, now.getYear(), now.getMonthValue());
                        String msg = String.format("Overall monthly budget: %s %s | Total Spent: %s %s | Remaining: %s %s (%.1f%% used).",
                                currency, summary.getTotalBudget(), currency, summary.getTotalBudgetSpent(), currency, summary.getRemainingBudget(), summary.getOverallBudgetPercentage());
                        return ToolResult.success(toolName, "finance", msg, summary);
                    }
                }

                // --- Task Tools ---
                case "createTask": {
                    String title = (String) args.get("title");
                    if (title == null || title.isBlank()) {
                        return ToolResult.error(toolName, "tasks", "Task title cannot be empty.");
                    }
                    String desc = (String) args.get("description");
                    String priorityStr = (String) args.getOrDefault("priority", "MEDIUM");
                    TaskPriority priority = TaskPriority.fromString(priorityStr);
                    String category = (String) args.get("category");

                    Instant dueDate = null;
                    if (args.get("dueDate") != null) {
                        try {
                            dueDate = Instant.parse(args.get("dueDate").toString());
                        } catch (Exception ignored) {
                            dueDate = LocalDate.now().plusDays(1).atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant();
                        }
                    }

                    TaskRequest req = TaskRequest.builder()
                            .title(title)
                            .description(desc)
                            .priority(priority)
                            .status(TaskStatus.TODO)
                            .dueDate(dueDate)
                            .category(category)
                            .build();

                    TaskResponse res = taskService.createTask(userId, req);
                    String msg = String.format("Created task '%s' [Priority: %s%s].",
                            res.getTitle(), res.getPriority(), res.getDueDate() != null ? ", Due: " + res.getDueDate() : "");
                    return ToolResult.success(toolName, "tasks", msg, res);
                }

                case "getTasks": {
                    String statusStr = (String) args.get("status");
                    TaskStatus status = statusStr != null ? TaskStatus.fromString(statusStr) : null;
                    String priorityStr = (String) args.get("priority");
                    TaskPriority priority = priorityStr != null ? TaskPriority.fromString(priorityStr) : null;

                    var res = taskService.getTasks(userId, status, priority, null, 0, 10);
                    String msg = String.format("Retrieved %d tasks.", res.getTotalElements());
                    return ToolResult.success(toolName, "tasks", msg, res.getContent());
                }

                case "getTodayTasks": {
                    List<TaskResponse> todayTasks = taskService.getTodayTasks(userId);
                    String msg = String.format("You have %d task(s) scheduled for today.", todayTasks.size());
                    return ToolResult.success(toolName, "tasks", msg, todayTasks);
                }

                case "getOverdueTasks": {
                    List<TaskResponse> overdue = taskService.getOverdueTasks(userId);
                    String msg = String.format("You have %d overdue task(s).", overdue.size());
                    return ToolResult.success(toolName, "tasks", msg, overdue);
                }

                case "completeTask": {
                    String taskId = (String) args.get("taskId");
                    if (taskId != null && !taskId.isBlank()) {
                        TaskResponse res = taskService.updateStatus(userId, taskId, TaskStatus.COMPLETED);
                        return ToolResult.success(toolName, "tasks", String.format("Marked task '%s' as completed.", res.getTitle()), res);
                    }
                    String title = (String) args.get("title");
                    if (title != null && !title.isBlank()) {
                        var tasks = taskService.getTasks(userId, null, null, title, 0, 1);
                        if (!tasks.getContent().isEmpty()) {
                            TaskResponse target = tasks.getContent().get(0);
                            TaskResponse res = taskService.updateStatus(userId, target.getId(), TaskStatus.COMPLETED);
                            return ToolResult.success(toolName, "tasks", String.format("Marked task '%s' as completed.", res.getTitle()), res);
                        }
                    }
                    return ToolResult.error(toolName, "tasks", "Could not find task to mark completed.");
                }

                // --- Travel Tools ---
                case "createTrip": {
                    String name = (String) args.get("name");
                    String destination = (String) args.get("destination");
                    String startStr = (String) args.get("startDate");
                    String endStr = (String) args.get("endDate");
                    BigDecimal budget = parseBigDecimal(args.get("budget"));

                    LocalDate start = startStr != null ? LocalDate.parse(startStr) : LocalDate.now().plusDays(7);
                    LocalDate end = endStr != null ? LocalDate.parse(endStr) : start.plusDays(3);

                    TripRequest req = TripRequest.builder()
                            .name(name != null ? name : destination + " Trip")
                            .destination(destination)
                            .startDate(start)
                            .endDate(end)
                            .budget(budget != null ? budget : BigDecimal.ZERO)
                            .currency(currency)
                            .status(TripStatus.PLANNED)
                            .build();

                    TripResponse res = tripService.createTrip(userId, req);
                    String msg = String.format("Created trip '%s' to %s (%s to %s, Budget: %s %s).",
                            res.getName(), res.getDestination(), res.getStartDate(), res.getEndDate(), currency, res.getBudget());
                    return ToolResult.success(toolName, "travel", msg, res);
                }

                case "generateTripPlan": {
                    String destination = (String) args.get("destination");
                    if (destination == null || destination.isBlank()) {
                        return ToolResult.error(toolName, "travel", "Destination must be specified.");
                    }
                    int days = args.get("days") != null ? ((Number) args.get("days")).intValue() : 3;
                    int travelers = args.get("travelers") != null ? ((Number) args.get("travelers")).intValue() : 1;
                    BigDecimal budget = parseBigDecimal(args.get("budget"));
                    String preferences = (String) args.get("preferences");

                    GenerateTripPlanRequest req = GenerateTripPlanRequest.builder()
                            .destination(destination)
                            .days(days)
                            .travelers(travelers)
                            .budget(budget)
                            .preferences(preferences)
                            .startDate(LocalDate.now().plusDays(7))
                            .build();

                    TripResponse res = travelService.generateTripPlan(userId, req);
                    String msg = String.format("Generated a %d-day itinerary for %s with %d scheduled activities under %s %s!",
                            days, destination, res.getItineraryItems().size(), currency, res.getBudget());
                    return ToolResult.success(toolName, "travel", msg, res);
                }

                case "getTrips": {
                    var res = tripService.getTrips(userId, 0, 5);
                    String msg = String.format("Found %d trip(s).", res.getTotalElements());
                    return ToolResult.success(toolName, "travel", msg, res.getContent());
                }

                default:
                    return ToolResult.error(toolName, "system", "Unknown tool: " + toolName);
            }
        } catch (Exception ex) {
            log.error("Error executing tool {}: ", toolName, ex);
            return ToolResult.error(toolName, "system", "Failed to execute tool: " + ex.getMessage());
        }
    }

    private BigDecimal parseBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        try {
            String str = obj.toString().replaceAll("[^0-9.]", "");
            return new BigDecimal(str);
        } catch (Exception e) {
            return null;
        }
    }
}
