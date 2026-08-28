package com.personalassistant;

import com.personalassistant.finance.*;
import com.personalassistant.user.User;
import com.personalassistant.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userRepository.findByEmail("demo@personalai.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Test User")
                        .email("test@example.com")
                        .passwordHash("hashed")
                        .currency("INR")
                        .timezone("Asia/Kolkata")
                        .build()));
    }

    @Test
    void testCreateExpenseAndCalculateBudget() {
        ExpenseRequest req = ExpenseRequest.builder()
                .amount(BigDecimal.valueOf(750))
                .category(ExpenseCategory.FOOD)
                .description("Team Dinner")
                .paymentMethod(PaymentMethod.UPI)
                .transactionDate(Instant.now())
                .build();

        ExpenseResponse created = expenseService.createExpense(testUser.getId(), req);
        assertNotNull(created.getId());
        assertEquals(BigDecimal.valueOf(750), created.getAmount());

        LocalDate now = LocalDate.now();
        BudgetRequest bReq = BudgetRequest.builder()
                .category(ExpenseCategory.FOOD)
                .amount(BigDecimal.valueOf(10000))
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();

        BudgetResponse budgetRes = budgetService.setBudget(testUser.getId(), bReq);
        assertTrue(budgetRes.getSpentAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
