package com.personalassistant.finance;

import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    @Transactional
    public ExpenseResponse createExpense(String userId, ExpenseRequest request) {
        User user = userService.getUserById(userId);

        Expense expense = Expense.builder()
                .user(user)
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription().trim())
                .paymentMethod(request.getPaymentMethod())
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : Instant.now())
                .build();

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(String userId, String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> getExpenses(
            String userId, ExpenseCategory category, Instant startDate, Instant endDate, int page, int size
    ) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage;

        if (category != null) {
            expensePage = expenseRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category, pageable);
        } else if (startDate != null && endDate != null) {
            expensePage = expenseRepository.findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
                    user, startDate, endDate, pageable
            );
        } else {
            expensePage = expenseRepository.findByUserOrderByTransactionDateDesc(user, pageable);
        }

        List<ExpenseResponse> content = expensePage.getContent().stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        return PagedResponse.<ExpenseResponse>builder()
                .content(content)
                .page(expensePage.getNumber())
                .size(expensePage.getSize())
                .totalElements(expensePage.getTotalElements())
                .totalPages(expensePage.getTotalPages())
                .last(expensePage.isLast())
                .build();
    }

    @Transactional
    public ExpenseResponse updateExpense(String userId, String expenseId, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription().trim());
        expense.setPaymentMethod(request.getPaymentMethod());
        if (request.getTransactionDate() != null) {
            expense.setTransactionDate(request.getTransactionDate());
        }

        Expense updated = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteExpense(String userId, String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlySpending(String userId, int year, int month) {
        User user = userService.getUserById(userId);
        YearMonth yearMonth = YearMonth.of(year, month);
        Instant start = yearMonth.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();

        return expenseRepository.sumTotalByUserAndDateBetween(user, start, end);
    }
}
