package com.personalassistant.finance;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ExpenseResponse>>> getExpenses(
            @RequestParam(required = false) ExpenseCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        PagedResponse<ExpenseResponse> response = expenseService.getExpenses(userId, category, startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@Valid @RequestBody ExpenseRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.createExpense(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Expense created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.getExpenseById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        ExpenseResponse response = expenseService.updateExpense(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        expenseService.deleteExpense(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }
}
