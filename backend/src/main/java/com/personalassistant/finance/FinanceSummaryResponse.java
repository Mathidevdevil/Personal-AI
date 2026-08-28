package com.personalassistant.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalBudget;
    private BigDecimal totalBudgetSpent;
    private BigDecimal remainingBudget;
    private double overallBudgetPercentage;
    private List<CategorySpendingResponse> categoryBreakdown;
    private List<MonthlySpendingResponse> monthlyTrends;
    private List<ExpenseResponse> recentTransactions;
}
