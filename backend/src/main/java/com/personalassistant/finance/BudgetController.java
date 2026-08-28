package com.personalassistant.finance;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();

        List<BudgetResponse> response = budgetService.getBudgets(userId, targetMonth, targetYear);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> setBudget(@Valid @RequestBody BudgetRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        BudgetResponse response = budgetService.setBudget(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Budget updated successfully", response), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
