package com.personalassistant.data;

import com.personalassistant.finance.*;
import com.personalassistant.notification.NotificationService;
import com.personalassistant.notification.NotificationType;
import com.personalassistant.task.Task;
import com.personalassistant.task.TaskPriority;
import com.personalassistant.task.TaskRepository;
import com.personalassistant.task.TaskStatus;
import com.personalassistant.travel.*;
import com.personalassistant.user.User;
import com.personalassistant.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final TaskRepository taskRepository;
    private final TripRepository tripRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail("demo@personalai.com").isPresent()) {
            log.info("Demo user already initialized.");
            return;
        }

        log.info("Initializing demo seed data...");

        // 1. Demo User
        User demoUser = User.builder()
                .name("Alex Mercer")
                .email("demo@personalai.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .currency("INR")
                .timezone("Asia/Kolkata")
                .build();
        User user = userRepository.save(demoUser);

        LocalDate now = LocalDate.now();
        int curYear = now.getYear();
        int curMonth = now.getMonthValue();

        // 2. Incomes
        incomeRepository.save(Income.builder()
                .user(user)
                .amount(BigDecimal.valueOf(85000))
                .source("Monthly Salary")
                .description("Tech Lead consulting salary")
                .incomeDate(Instant.now().minus(Duration.ofDays(5)))
                .build());

        incomeRepository.save(Income.builder()
                .user(user)
                .amount(BigDecimal.valueOf(15000))
                .source("Freelance Project")
                .description("UI Architecture Consultation")
                .incomeDate(Instant.now().minus(Duration.ofDays(2)))
                .build());

        // 3. Budgets
        budgetRepository.save(Budget.builder().user(user).category(ExpenseCategory.FOOD).amount(BigDecimal.valueOf(15000)).month(curMonth).year(curYear).build());
        budgetRepository.save(Budget.builder().user(user).category(ExpenseCategory.TRANSPORT).amount(BigDecimal.valueOf(5000)).month(curMonth).year(curYear).build());
        budgetRepository.save(Budget.builder().user(user).category(ExpenseCategory.SHOPPING).amount(BigDecimal.valueOf(10000)).month(curMonth).year(curYear).build());
        budgetRepository.save(Budget.builder().user(user).category(ExpenseCategory.BILLS).amount(BigDecimal.valueOf(8000)).month(curMonth).year(curYear).build());
        budgetRepository.save(Budget.builder().user(user).category(ExpenseCategory.ENTERTAINMENT).amount(BigDecimal.valueOf(4000)).month(curMonth).year(curYear).build());

        // 4. Expenses
        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(BigDecimal.valueOf(500))
                .category(ExpenseCategory.FOOD)
                .description("Dinner with team")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(Instant.now().minus(Duration.ofHours(4)))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(BigDecimal.valueOf(250))
                .category(ExpenseCategory.TRANSPORT)
                .description("Cab ride to tech park")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(Instant.now().minus(Duration.ofHours(10)))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(BigDecimal.valueOf(1200))
                .category(ExpenseCategory.SHOPPING)
                .description("Ergonomic desk accessories")
                .paymentMethod(PaymentMethod.CARD)
                .transactionDate(Instant.now().minus(Duration.ofDays(1)))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(BigDecimal.valueOf(2400))
                .category(ExpenseCategory.BILLS)
                .description("Gigabit fiber broadband bill")
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .transactionDate(Instant.now().minus(Duration.ofDays(3)))
                .build());

        expenseRepository.save(Expense.builder()
                .user(user)
                .amount(BigDecimal.valueOf(650))
                .category(ExpenseCategory.ENTERTAINMENT)
                .description("Sci-Fi IMAX movie tickets")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(Instant.now().minus(Duration.ofDays(4)))
                .build());

        // 5. Tasks
        taskRepository.save(Task.builder()
                .user(user)
                .title("Complete resume and portfolio review")
                .description("Update recent AI orchestration projects and publish portfolio updates.")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.TODO)
                .dueDate(Instant.now().plus(Duration.ofDays(1)))
                .category("Career")
                .build());

        taskRepository.save(Task.builder()
                .user(user)
                .title("Submit tax declaration documentation")
                .description("Verify deductions under Section 80C and upload investment proof.")
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.TODO)
                .dueDate(Instant.now().minus(Duration.ofHours(12))) // Overdue on purpose for testing
                .category("Finance")
                .build());

        taskRepository.save(Task.builder()
                .user(user)
                .title("Prepare architecture presentation for sprint review")
                .description("Finalize state machine sequence diagrams and API schemas.")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(Instant.now().plus(Duration.ofDays(3)))
                .category("Work")
                .build());

        taskRepository.save(Task.builder()
                .user(user)
                .title("Review monthly personal investment allocations")
                .description("Rebalance mutual funds and index ETF portfolio.")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .completedAt(Instant.now().minus(Duration.ofDays(1)))
                .category("Finance")
                .build());

        // 6. Trip
        Trip trip = Trip.builder()
                .user(user)
                .name("Bangalore Weekend Exploration")
                .destination("Bangalore")
                .startDate(now.plusDays(10))
                .endDate(now.plusDays(12))
                .budget(BigDecimal.valueOf(15000))
                .currency("INR")
                .description("Exploring botanical gardens, heritage palaces, and craft cafes.")
                .status(TripStatus.PLANNED)
                .build();

        List<ItineraryItem> items = new ArrayList<>();
        items.add(ItineraryItem.builder().trip(trip).dayNumber(1).title("Cubbon Park & State Library Walk").description("Morning stroll through shaded bamboo groves.").location("Cubbon Park").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(11, 0)).estimatedCost(BigDecimal.valueOf(200)).notes("Comfortable shoes").build());
        items.add(ItineraryItem.builder().trip(trip).dayNumber(1).title("Traditional Dosa & Vidhana Soudha").description("Iconic lunch & photo stop at the state legislature.").location("MG Road").startTime(LocalTime.of(12, 30)).endTime(LocalTime.of(15, 0)).estimatedCost(BigDecimal.valueOf(800)).notes("Try filter coffee").build());
        items.add(ItineraryItem.builder().trip(trip).dayNumber(1).title("Indiranagar Cafe & Culinary Tour").description("Artisanal dinner and craft mocktails.").location("100ft Road").startTime(LocalTime.of(18, 30)).endTime(LocalTime.of(21, 30)).estimatedCost(BigDecimal.valueOf(2500)).notes("Reserve table").build());
        items.add(ItineraryItem.builder().trip(trip).dayNumber(2).title("Lalbagh Botanical Garden & Glasshouse").description("Century-old botanical collection & flower gardens.").location("Lalbagh").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(12, 0)).estimatedCost(BigDecimal.valueOf(300)).notes("Morning bloom time").build());
        items.add(ItineraryItem.builder().trip(trip).dayNumber(2).title("Bangalore Palace Royal Heritage Tour").description("Explore Tudor-style architecture and art collection.").location("Palace Grounds").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(17, 0)).estimatedCost(BigDecimal.valueOf(1200)).notes("Audio guide included").build());
        items.add(ItineraryItem.builder().trip(trip).dayNumber(3).title("Commercial Street & Souvenir Shopping").description("Browse local textiles, spices, and handcrafted souvenirs.").location("Commercial Street").startTime(LocalTime.of(10, 30)).endTime(LocalTime.of(14, 0)).estimatedCost(BigDecimal.valueOf(3500)).notes("UPI accepted widely").build());

        trip.setItineraryItems(items);
        tripRepository.save(trip);

        // 7. Notifications
        notificationService.createNotification(user, "Welcome to Personal AI!", "Your financial tracker, tasks, and travel planner are ready to explore.", NotificationType.SYSTEM);
        notificationService.createNotification(user, "Urgent Task Overdue", "Task 'Submit tax declaration documentation' was due 12 hours ago.", NotificationType.TASK_OVERDUE);
        notificationService.createNotification(user, "Upcoming Bangalore Trip", "Your trip to Bangalore starts in 10 days! Check your itinerary.", NotificationType.TRIP_REMINDER);

        log.info("Demo data created successfully for demo@personalai.com / Password123!");
    }
}
