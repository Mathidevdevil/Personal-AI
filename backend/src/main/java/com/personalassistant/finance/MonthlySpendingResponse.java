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
public class MonthlySpendingResponse {
    private String monthName;
    private int month;
    private int year;
    private BigDecimal expense;
    private BigDecimal income;
    private BigDecimal netSavings;
}
