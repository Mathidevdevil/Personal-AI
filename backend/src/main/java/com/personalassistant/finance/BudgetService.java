package com.personalassistant.finance;

import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    @Transactional
    public BudgetResponse setBudget(String userId, BudgetRequest request) {
        User user = userService.getUserById(userId);

        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(
                user, request.getCategory(), request.getMonth(), request.getYear()
        ).orElse(Budget.builder()
                .user(user)
                .category(request.getCategory())
                .month(request.getMonth())
                .year(request.getYear())
                .build());

        budget.setAmount(request.getAmount());
        Budget saved = budgetRepository.save(budget);

        BigDecimal spent = calculateSpentForCategory(user, request.getCategory(), request.getMonth(), request.getYear());
        return BudgetResponse.fromEntity(saved, spent);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(String userId, int month, int year) {
        User user = userService.getUserById(userId);
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);

        return budgets.stream().map(budget -> {
            BigDecimal spent = calculateSpentForCategory(user, budget.getCategory(), month, year);
            return BudgetResponse.fromEntity(budget, spent);
        }).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetForCategory(String userId, ExpenseCategory category, int month, int year) {
        User user = userService.getUserById(userId);
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year)
                .orElse(Budget.builder()
                        .user(user)
                        .category(category)
                        .amount(BigDecimal.ZERO)
                        .month(month)
                        .year(year)
                        .build());

        BigDecimal spent = calculateSpentForCategory(user, category, month, year);
        return BudgetResponse.fromEntity(budget, spent);
    }

    @Transactional
    public void deleteBudget(String userId, String budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        budgetRepository.delete(budget);
    }

    private BigDecimal calculateSpentForCategory(User user, ExpenseCategory category, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        Instant start = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        return expenseRepository.sumByUserAndCategoryAndDateBetween(user, category, start, end);
    }
}
