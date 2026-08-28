package com.personalassistant.ai;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AIToolRegistry {

    private final List<AIToolDefinition> toolDefinitions = new ArrayList<>();

    public AIToolRegistry() {
        initTools();
    }

    public List<AIToolDefinition> getAllTools() {
        return toolDefinitions;
    }

    private void initTools() {
        // --- Finance Tools ---
        toolDefinitions.add(AIToolDefinition.builder()
                .name("createExpense")
                .description("Record a new expense with amount, category, description, and optional payment method")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "amount", Map.of("type", "number", "description", "Expense amount (e.g. 500)"),
                                "category", Map.of("type", "string", "description", "Expense category: FOOD, TRANSPORT, SHOPPING, BILLS, ENTERTAINMENT, EDUCATION, HEALTH, TRAVEL, OTHER"),
                                "description", Map.of("type", "string", "description", "Description of what was bought (e.g. Dinner with friends)"),
                                "paymentMethod", Map.of("type", "string", "description", "Payment method: CASH, UPI, CARD, BANK_TRANSFER, OTHER")
                        ),
                        "required", List.of("amount", "category", "description")
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("createIncome")
                .description("Record a new income source with amount and description")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "amount", Map.of("type", "number", "description", "Income amount"),
                                "source", Map.of("type", "string", "description", "Source of income (e.g. Salary, Freelance, Dividend)"),
                                "description", Map.of("type", "string", "description", "Optional details")
                        ),
                        "required", List.of("amount", "source")
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getExpenses")
                .description("Retrieve recent expenses, optionally filtered by category")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "category", Map.of("type", "string", "description", "Optional category filter")
                        )
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getMonthlySpending")
                .description("Get total spending for the current or specified month")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "month", Map.of("type", "integer", "description", "Month number (1-12)"),
                                "year", Map.of("type", "integer", "description", "Year (e.g. 2026)")
                        )
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getBudgetStatus")
                .description("Check budget limits, spent amount, and remaining budget for a category or all categories")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "category", Map.of("type", "string", "description", "Category to inspect: FOOD, TRANSPORT, etc.")
                        )
                ))
                .build());

        // --- Task Tools ---
        toolDefinitions.add(AIToolDefinition.builder()
                .name("createTask")
                .description("Create a new task with title, priority, due date, and category")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "title", Map.of("type", "string", "description", "Title of the task (e.g. Finish my resume)"),
                                "description", Map.of("type", "string", "description", "Detailed description"),
                                "priority", Map.of("type", "string", "description", "Priority: LOW, MEDIUM, HIGH, URGENT"),
                                "dueDate", Map.of("type", "string", "description", "ISO-8601 due date/time (e.g. 2026-08-29T18:00:00Z)"),
                                "category", Map.of("type", "string", "description", "Category: Career, Personal, Work, Health, etc.")
                        ),
                        "required", List.of("title")
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getTasks")
                .description("Retrieve tasks, optionally filtered by status (TODO, IN_PROGRESS, COMPLETED) or priority")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "status", Map.of("type", "string", "description", "Status filter"),
                                "priority", Map.of("type", "string", "description", "Priority filter")
                        )
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getTodayTasks")
                .description("Get all tasks scheduled for today")
                .parameters(Map.of("type", "object", "properties", Map.of()))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getOverdueTasks")
                .description("Get all overdue tasks that are past due date and not completed")
                .parameters(Map.of("type", "object", "properties", Map.of()))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("completeTask")
                .description("Mark a task as completed by task ID or title")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "taskId", Map.of("type", "string", "description", "The task ID to mark completed"),
                                "title", Map.of("type", "string", "description", "Task title if ID not known")
                        )
                ))
                .build());

        // --- Travel Tools ---
        toolDefinitions.add(AIToolDefinition.builder()
                .name("createTrip")
                .description("Create a new travel trip with destination, dates, and budget")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "name", Map.of("type", "string", "description", "Name of the trip"),
                                "destination", Map.of("type", "string", "description", "Destination city/country"),
                                "startDate", Map.of("type", "string", "description", "Start date (YYYY-MM-DD)"),
                                "endDate", Map.of("type", "string", "description", "End date (YYYY-MM-DD)"),
                                "budget", Map.of("type", "number", "description", "Total trip budget amount")
                        ),
                        "required", List.of("name", "destination", "startDate", "endDate")
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("generateTripPlan")
                .description("Generate an AI structured multi-day itinerary with activities and cost estimates")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "destination", Map.of("type", "string", "description", "Destination city (e.g. Bangalore, Goa, Paris)"),
                                "days", Map.of("type", "integer", "description", "Number of days (e.g. 3)"),
                                "travelers", Map.of("type", "integer", "description", "Number of travelers (e.g. 2)"),
                                "budget", Map.of("type", "number", "description", "Target budget in currency (e.g. 15000)"),
                                "preferences", Map.of("type", "string", "description", "Preferences: Food, Heritage, Adventure, Relaxation")
                        ),
                        "required", List.of("destination", "days")
                ))
                .build());

        toolDefinitions.add(AIToolDefinition.builder()
                .name("getTrips")
                .description("Retrieve all planned, active, and upcoming trips")
                .parameters(Map.of("type", "object", "properties", Map.of()))
                .build());
    }
}
