package com.example.finance.controller;

import com.example.finance.dto.request.AccountRequest;
import com.example.finance.dto.response.AccountResponse;
import com.example.finance.dto.response.ApiResponse;
import com.example.finance.security.UserPrincipal;
import com.example.finance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new account", description = "Creates a new financial account for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")})
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Valid @RequestBody
    AccountRequest request) {
        AccountResponse response = accountService.createAccount(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", response));
    }

    @Operation(summary = "Get all accounts", description = "Retrieves all accounts for the authenticated user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(@AuthenticationPrincipal
    UserPrincipal userPrincipal) {
        List<AccountResponse> accounts = accountService.getAllAccounts(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @Operation(summary = "Get account by ID", description = "Retrieves a specific account by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Account ID") @PathVariable
    Long id) {
        AccountResponse response = accountService.getAccountById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update account", description = "Updates an existing account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")})
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Account ID") @PathVariable
    Long id, @Valid @RequestBody
    AccountRequest request) {
        AccountResponse response = accountService.updateAccount(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", response));
    }

    @Operation(summary = "Delete account", description = "Deletes an account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Account not found")})
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal
    UserPrincipal userPrincipal, @Parameter(description = "Account ID") @PathVariable
    Long id) {
        accountService.deleteAccount(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @Operation(summary = "Get total balance", description = "Calculates the total balance across all accounts")
    @GetMapping("/total-balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBalance(@AuthenticationPrincipal
    UserPrincipal userPrincipal) {
        BigDecimal totalBalance = accountService.getTotalBalance(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(totalBalance));
    }
}
