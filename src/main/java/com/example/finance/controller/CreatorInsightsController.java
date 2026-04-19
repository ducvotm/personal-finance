package com.example.finance.controller;

import com.example.finance.dto.response.ApiResponse;
import com.example.finance.dto.response.SafeToSpendResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.SafeToSpendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/creator-insights")
@RequiredArgsConstructor
@Tag(name = "Creator Insights", description = "Creator-specific planning insights")
public class CreatorInsightsController {

    private final SafeToSpendService safeToSpendService;

    @Operation(summary = "Get monthly safe-to-spend", description = "Returns one safe-to-spend number based on income volatility and active budget commitments")
    @GetMapping("/safe-to-spend")
    public ResponseEntity<ApiResponse<SafeToSpendResponse>> getSafeToSpend(@AuthenticationPrincipal
    UserPrincipal userPrincipal,
            @Parameter(description = "Target month in yyyy-MM format (optional)") @RequestParam(required = false)
            String month) {
        YearMonth targetMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        SafeToSpendResponse response = safeToSpendService.calculateMonthlySafeToSpend(userPrincipal.getId(),
                targetMonth);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
