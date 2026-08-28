package com.personalassistant.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBasedFallbackAIService {

    private final AIToolDispatcher toolDispatcher;

    public AIResponse processIntent(AIRequest request) {
        String input = request.getPrompt().trim();
        String lower = input.toLowerCase();
        String userId = request.getUserId();

        List<AIToolCall> toolCalls = new ArrayList<>();
        List<ToolResult> toolResults = new ArrayList<>();
        StringBuilder replyBuilder = new StringBuilder();

        // 1. Expense Intent ("Add ₹500 for dinner", "Spent 250 on transport", "Expense 1200 shopping")
        if (matchesExpense(lower)) {
            BigDecimal amount = extractAmount(input);
            String category = extractCategory(lower);
            String description = extractDescription(input, amount, category);

            Map<String, Object> args = new HashMap<>();
            args.put("amount", amount != null ? amount : BigDecimal.valueOf(100));
            args.put("category", category);
            args.put("description", description);
            args.put("paymentMethod", lower.contains("cash") ? "CASH" : (lower.contains("card") ? "CARD" : "UPI"));

            AIToolCall tc = AIToolCall.builder()
                    .id("tool-" + UUID.randomUUID())
                    .toolName("createExpense")
                    .arguments(args)
                    .build();
            toolCalls.add(tc);

            ToolResult tr = toolDispatcher.executeTool(userId, "createExpense", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 2. Monthly Spending Query ("How much did I spend this month?", "Monthly expenses", "What is my spending?")
        else if (lower.contains("how much") && (lower.contains("spend") || lower.contains("spent")) || lower.contains("monthly spending") || lower.contains("total expense")) {
            Map<String, Object> args = Map.of();
            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("getMonthlySpending").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "getMonthlySpending", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 3. Budget Status Query ("What is my remaining food budget?", "How much money do I have left in my budget?", "Budget status")
        else if (lower.contains("budget") && (lower.contains("left") || lower.contains("remaining") || lower.contains("status") || lower.contains("how much"))) {
            String cat = extractCategory(lower);
            Map<String, Object> args = cat.equals("OTHER") ? Map.of() : Map.of("category", cat);
            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("getBudgetStatus").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "getBudgetStatus", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 4. Overdue Tasks ("Show my overdue tasks", "What tasks are overdue?", "Overdue tasks")
        else if (lower.contains("overdue")) {
            Map<String, Object> args = Map.of();
            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("getOverdueTasks").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "getOverdueTasks", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 5. Today's Tasks / Priorities ("What should I prioritize today?", "Today's tasks", "What's important today?")
        else if (lower.contains("prioritize") || lower.contains("today's task") || lower.contains("tasks today") || (lower.contains("what") && lower.contains("today"))) {
            Map<String, Object> args = Map.of();
            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("getTodayTasks").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "getTodayTasks", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 6. Create Task ("Create a task to finish my resume tomorrow", "Add task buy groceries", "Remind me to submit assignment")
        else if (matchesTaskCreation(lower)) {
            String title = extractTaskTitle(input);
            String dueDate = null;
            if (lower.contains("tomorrow")) {
                dueDate = LocalDate.now().plusDays(1).atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toString();
            } else if (lower.contains("today")) {
                dueDate = LocalDate.now().atTime(20, 0).atZone(ZoneId.systemDefault()).toInstant().toString();
            } else if (lower.contains("next week")) {
                dueDate = LocalDate.now().plusDays(7).atTime(18, 0).atZone(ZoneId.systemDefault()).toInstant().toString();
            }

            String priority = (lower.contains("urgent") || lower.contains("asap")) ? "URGENT" : (lower.contains("high") || lower.contains("important") ? "HIGH" : "MEDIUM");

            Map<String, Object> args = new HashMap<>();
            args.put("title", title);
            args.put("priority", priority);
            if (dueDate != null) args.put("dueDate", dueDate);

            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("createTask").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "createTask", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 7. Complete Task ("Complete task finish resume", "Mark task completed", "Done with resume")
        else if (lower.startsWith("complete task") || lower.startsWith("finish task") || lower.startsWith("mark task")) {
            String title = input.replaceAll("(?i)^(complete|finish|mark)\\s+(task|as\\s+done)\\s*", "").trim();
            Map<String, Object> args = Map.of("title", title);
            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("completeTask").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "completeTask", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 8. Trip Generation / Planning ("Plan a 3-day Bangalore trip under ₹15,000", "Plan trip to Goa", "Create an itinerary for my trip")
        else if (lower.contains("plan") && (lower.contains("trip") || lower.contains("itinerary")) || lower.contains("itinerary")) {
            String destination = extractDestination(input);
            int days = extractDays(input);
            BigDecimal budget = extractAmount(input);
            int travelers = lower.contains("for 2") || lower.contains("2 people") ? 2 : 1;

            Map<String, Object> args = new HashMap<>();
            args.put("destination", destination);
            args.put("days", days);
            args.put("travelers", travelers);
            if (budget != null) args.put("budget", budget);

            AIToolCall tc = AIToolCall.builder().id("tool-" + UUID.randomUUID()).toolName("generateTripPlan").arguments(args).build();
            toolCalls.add(tc);
            ToolResult tr = toolDispatcher.executeTool(userId, "generateTripPlan", args);
            toolResults.add(tr);
            replyBuilder.append(tr.getMessage());
        }
        // 9. General Conversation / Help / Overview
        else {
            replyBuilder.append("I am your Personal AI Assistant! I can help you seamlessly manage your finances, tasks, and travel.\n\n")
                    .append("Here are some things you can ask me:\n")
                    .append("• **\"Add ₹500 for dinner\"** (Records an expense)\n")
                    .append("• **\"How much did I spend this month?\"** (Calculates your current spending)\n")
                    .append("• **\"What is my remaining food budget?\"** (Checks budget status)\n")
                    .append("• **\"Create a task to finish my resume tomorrow\"** (Schedules a task)\n")
                    .append("• **\"Show my overdue tasks\"** (Highlights urgent deadlines)\n")
                    .append("• **\"Plan a 3-day Bangalore trip under ₹15,000\"** (Generates a structured itinerary)");
        }

        return AIResponse.builder()
                .reply(replyBuilder.toString())
                .toolCalls(toolCalls)
                .toolResults(toolResults)
                .build();
    }

    private boolean matchesExpense(String lower) {
        return (lower.contains("add") || lower.contains("spent") || lower.contains("paid") || lower.contains("expense") || lower.contains("bought"))
                && (lower.contains("₹") || lower.contains("rs") || lower.contains("inr") || lower.contains("$") || lower.matches(".*\\b\\d+\\b.*\\b(for|on|in)\\b.*"));
    }

    private boolean matchesTaskCreation(String lower) {
        return lower.contains("create a task") || lower.contains("create task") || lower.contains("add task")
                || lower.contains("remind me to") || lower.contains("new task") || lower.startsWith("task:");
    }

    private BigDecimal extractAmount(String input) {
        Pattern pattern = Pattern.compile("(?:[₹$€£]|rs\\.?|inr)?\\s*(\\d+(?:,\\d+)*(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            try {
                String clean = matcher.group(1).replace(",", "");
                return new BigDecimal(clean);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractCategory(String lower) {
        if (lower.contains("dinner") || lower.contains("lunch") || lower.contains("food") || lower.contains("grocery") || lower.contains("coffee") || lower.contains("snack") || lower.contains("restaurant")) return "FOOD";
        if (lower.contains("transport") || lower.contains("cab") || lower.contains("uber") || lower.contains("ola") || lower.contains("petrol") || lower.contains("fuel") || lower.contains("flight") || lower.contains("train") || lower.contains("metro") || lower.contains("bus")) return "TRANSPORT";
        if (lower.contains("shopping") || lower.contains("clothes") || lower.contains("shoes") || lower.contains("amazon") || lower.contains("flipkart")) return "SHOPPING";
        if (lower.contains("bill") || lower.contains("electricity") || lower.contains("wifi") || lower.contains("internet") || lower.contains("recharge") || lower.contains("rent")) return "BILLS";
        if (lower.contains("movie") || lower.contains("cinema") || lower.contains("game") || lower.contains("party") || lower.contains("entertainment") || lower.contains("netflix")) return "ENTERTAINMENT";
        if (lower.contains("book") || lower.contains("course") || lower.contains("tuition") || lower.contains("education")) return "EDUCATION";
        if (lower.contains("doctor") || lower.contains("medicine") || lower.contains("health") || lower.contains("pharmacy") || lower.contains("gym")) return "HEALTH";
        if (lower.contains("trip") || lower.contains("travel") || lower.contains("hotel") || lower.contains("tour")) return "TRAVEL";
        return "OTHER";
    }

    private String extractDescription(String input, BigDecimal amount, String category) {
        String clean = input.replaceAll("(?i)^(add|spent|paid|record|expense)\\s*", "");
        if (amount != null) {
            clean = clean.replaceAll("(?i)(?:[₹$€£]|rs\\.?|inr)?\\s*" + amount.intValue(), "");
        }
        clean = clean.replaceAll("(?i)\\b(for|on|in|via|using|upi|cash|card)\\b", "").trim();
        if (clean.length() < 2) {
            return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase() + " Expense";
        }
        return clean.substring(0, 1).toUpperCase() + clean.substring(1);
    }

    private String extractTaskTitle(String input) {
        String clean = input.replaceAll("(?i)^(create\\s+a\\s+task\\s+(to)?|create\\s+task\\s+(to)?|add\\s+task\\s+(to)?|remind\\s+me\\s+to|task:?)\\s*", "").trim();
        clean = clean.replaceAll("(?i)\\b(tomorrow|today|tonight|next week|asap|urgent|urgently)\\b", "").trim();
        if (clean.length() < 2) return "New Task";
        return clean.substring(0, 1).toUpperCase() + clean.substring(1);
    }

    private String extractDestination(String input) {
        Pattern pattern = Pattern.compile("(?i)(?:to|for|in)\\s+([a-zA-Z\\s]+?)(?:\\s+trip|\\s+under|\\s+for|$)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String dest = matcher.group(1).trim();
            if (!dest.isEmpty() && !dest.equalsIgnoreCase("my") && !dest.equalsIgnoreCase("the")) {
                return dest;
            }
        }
        if (input.toLowerCase().contains("bangalore")) return "Bangalore";
        if (input.toLowerCase().contains("goa")) return "Goa";
        if (input.toLowerCase().contains("mumbai")) return "Mumbai";
        if (input.toLowerCase().contains("delhi")) return "Delhi";
        if (input.toLowerCase().contains("paris")) return "Paris";
        if (input.toLowerCase().contains("tokyo")) return "Tokyo";
        return "Bangalore";
    }

    private int extractDays(String input) {
        Pattern pattern = Pattern.compile("(\\d+)[-\\s]*day", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (Exception ignored) {}
        }
        return 3;
    }
}
