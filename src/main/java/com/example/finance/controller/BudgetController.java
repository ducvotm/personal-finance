package com.example.finance.controller;

import com.example.finance.dto.request.BudgetRequest;
import com.example.finance.dto.response.ApiResponse;
import com.example.finance.dto.response.BudgetResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Budget management endpoints")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "Create a new budget", description = "Creates a new budget for a category")
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Valid @RequestBody
    BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Budget created successfully", response));
    }

    @Operation(summary = "Get all budgets", description = "Retrieves all active budgets for the user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(@AuthenticationPrincipal
    UserPrincipal userPrincipal) {
        List<BudgetResponse> budgets = budgetService.getAllBudgets(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @Operation(summary = "Get budgets by date range", description = "Retrieves budgets for a specific date range")
    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsByDateRange(@AuthenticationPrincipal
    UserPrincipal userPrincipal,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        List<BudgetResponse> budgets = budgetService.getBudgetsByDateRange(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @Operation(summary = "Get budget by ID", description = "Retrieves a specific budget by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Budget ID") @PathVariable
    Long id) {
        BudgetResponse response = budgetService.getBudgetById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update budget", description = "Updates an existing budget")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Budget ID") @PathVariable
    Long id, @Valid @RequestBody
    BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @Operation(summary = "Delete budget", description = "Deactivates a budget (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Budget ID") @PathVariable
    Long id) {
        budgetService.deleteBudget(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}