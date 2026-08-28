package com.personalassistant.finance;

import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceDashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary(String userId, int year, int month) {
        User user = userService.getUserById(userId);

        BigDecimal totalIncome = incomeRepository.sumTotalByUser(user);
        BigDecimal totalExpenses = expenseRepository.sumTotalByUser(user);
        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        YearMonth currentYm = YearMonth.of(year, month);
        Instant monthStart = currentYm.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant monthEnd = currentYm.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();

        BigDecimal monthlyIncome = incomeRepository.sumTotalByUserAndDateBetween(user, monthStart, monthEnd);
        BigDecimal monthlyExpenses = expenseRepository.sumTotalByUserAndDateBetween(user, monthStart, monthEnd);

        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);
        Map<ExpenseCategory, BigDecimal> budgetMap = new HashMap<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        for (Budget b : budgets) {
            budgetMap.put(b.getCategory(), b.getAmount());
            totalBudget = totalBudget.add(b.getAmount());
        }

        // Category breakdown
        List<CategorySpendingResponse> categoryBreakdown = new ArrayList<>();
        BigDecimal totalBudgetSpent = BigDecimal.ZERO;

        for (ExpenseCategory cat : ExpenseCategory.values()) {
            BigDecimal spent = expenseRepository.sumByUserAndCategoryAndDateBetween(user, cat, monthStart, monthEnd);
            BigDecimal budget = budgetMap.getOrDefault(cat, BigDecimal.ZERO);

            if (spent.compareTo(BigDecimal.ZERO) > 0 || budget.compareTo(BigDecimal.ZERO) > 0) {
                totalBudgetSpent = totalBudgetSpent.add(spent);
                BigDecimal remaining = budget.subtract(spent);

                double pctOfTotal = monthlyExpenses.compareTo(BigDecimal.ZERO) > 0
                        ? spent.multiply(BigDecimal.valueOf(100)).divide(monthlyExpenses, 2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;

                double budgetUsage = budget.compareTo(BigDecimal.ZERO) > 0
                        ? spent.multiply(BigDecimal.valueOf(100)).divide(budget, 2, RoundingMode.HALF_UP).doubleValue()
                        : 0.0;

                categoryBreakdown.add(CategorySpendingResponse.builder()
                        .category(cat)
                        .spentAmount(spent)
                        .budgetAmount(budget)
                        .remainingAmount(remaining)
                        .percentageOfTotal(pctOfTotal)
                        .budgetUsagePercentage(budgetUsage)
                        .build());
            }
        }

        categoryBreakdown.sort((a, b) -> b.getSpentAmount().compareTo(a.getSpentAmount()));

        BigDecimal remainingBudget = totalBudget.subtract(totalBudgetSpent);
        double overallBudgetPct = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalBudgetSpent.multiply(BigDecimal.valueOf(100)).divide(totalBudget, 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Monthly trends (past 6 months)
        List<MonthlySpendingResponse> monthlyTrends = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYm.minusMonths(i);
            Instant start = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();

            BigDecimal exp = expenseRepository.sumTotalByUserAndDateBetween(user, start, end);
            BigDecimal inc = incomeRepository.sumTotalByUserAndDateBetween(user, start, end);

            monthlyTrends.add(MonthlySpendingResponse.builder()
                    .monthName(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .month(ym.getMonthValue())
                    .year(ym.getYear())
                    .expense(exp)
                    .income(inc)
                    .netSavings(inc.subtract(exp))
                    .build());
        }

        // Recent 5 transactions
        List<ExpenseResponse> recent = expenseRepository.findTop5ByUserOrderByTransactionDateDesc(user).stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        return FinanceSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .currentBalance(currentBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .totalBudget(totalBudget)
                .totalBudgetSpent(totalBudgetSpent)
                .remainingBudget(remainingBudget)
                .overallBudgetPercentage(overallBudgetPct)
                .categoryBreakdown(categoryBreakdown)
                .monthlyTrends(monthlyTrends)
                .recentTransactions(recent)
                .build();
    }
}
