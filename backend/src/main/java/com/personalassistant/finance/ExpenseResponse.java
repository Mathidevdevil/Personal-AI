package com.personalassistant.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private String id;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String description;
    private PaymentMethod paymentMethod;
    private Instant transactionDate;
    private Instant createdAt;
    private Instant updatedAt;

    public static ExpenseResponse fromEntity(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .paymentMethod(expense.getPaymentMethod())
                .transactionDate(expense.getTransactionDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
