package com.personalassistant.finance;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance/dashboard")
@RequiredArgsConstructor
public class FinanceDashboardController {

    private final FinanceDashboardService financeDashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<FinanceSummaryResponse>> getDashboardSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();
        int targetMonth = month != null ? month : now.getMonthValue();
        int targetYear = year != null ? year : now.getYear();

        FinanceSummaryResponse summary = financeDashboardService.getSummary(userId, targetYear, targetMonth);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
