package com.personalassistant.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpendingResponse {
    private ExpenseCategory category;
    private BigDecimal spentAmount;
    private BigDecimal budgetAmount;
    private BigDecimal remainingAmount;
    private double percentageOfTotal;
    private double budgetUsagePercentage;
}
