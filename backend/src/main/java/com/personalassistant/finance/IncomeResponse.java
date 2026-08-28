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
public class IncomeResponse {
    private String id;
    private BigDecimal amount;
    private String source;
    private String description;
    private Instant incomeDate;
    private Instant createdAt;

    public static IncomeResponse fromEntity(Income income) {
        return IncomeResponse.builder()
                .id(income.getId())
                .amount(income.getAmount())
                .source(income.getSource())
                .description(income.getDescription())
                .incomeDate(income.getIncomeDate())
                .createdAt(income.getCreatedAt())
                .build();
    }
}
