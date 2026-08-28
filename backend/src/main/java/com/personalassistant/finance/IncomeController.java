package com.personalassistant.finance;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<IncomeResponse>>> getIncome(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        PagedResponse<IncomeResponse> response = incomeService.getIncome(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncomeResponse>> createIncome(@Valid @RequestBody IncomeRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        IncomeResponse response = incomeService.createIncome(userId, request);
        return new ResponseEntity<>(ApiResponse.success("Income recorded successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> getIncomeById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        IncomeResponse response = incomeService.getIncomeById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            @PathVariable String id,
            @Valid @RequestBody IncomeRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        IncomeResponse response = incomeService.updateIncome(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Income updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        incomeService.deleteIncome(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Income deleted successfully", null));
    }
}
