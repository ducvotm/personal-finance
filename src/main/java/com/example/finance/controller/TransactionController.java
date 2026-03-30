package com.example.finance.controller;

import com.example.finance.dto.request.TransactionRequest;
import com.example.finance.dto.response.ApiResponse;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create a new transaction", description = "Creates a new income or expense transaction")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")})
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Valid @RequestBody
    TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Transaction created successfully", response));
    }

    @Operation(summary = "Get all transactions", description = "Retrieves paginated list of transactions with sorting options")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0")
    int page, @Parameter(description = "Page size") @RequestParam(defaultValue = "20")
    int size, @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "transactionDate")
    String sortBy, @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "desc")
    String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Transaction ID") @PathVariable
    Long id) {
        TransactionResponse response = transactionService.getTransactionById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update transaction", description = "Updates an existing transaction")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Transaction ID") @PathVariable
    Long id, @Valid @RequestBody
    TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", response));
    }

    @Operation(summary = "Delete transaction", description = "Deletes a transaction")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Transaction ID") @PathVariable
    Long id) {
        transactionService.deleteTransaction(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }

    @Operation(summary = "Get transactions by date range", description = "Retrieves all transactions within a specified date range")
    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByDateRange(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam
    LocalDate startDate, @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam
    LocalDate endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByDateRange(userPrincipal.getId(),
                startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @Operation(summary = "Get transaction summary", description = "Calculates total income, expense, and net balance for a date range")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getTransactionSummary(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam
    LocalDate startDate, @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam
    LocalDate endDate) {
        BigDecimal totalIncome = transactionService.getTotalIncome(userPrincipal.getId(), startDate, endDate);
        BigDecimal totalExpense = transactionService.getTotalExpense(userPrincipal.getId(), startDate, endDate);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", totalIncome.subtract(totalExpense));

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
