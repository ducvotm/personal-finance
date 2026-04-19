package com.example.finance.service;

import com.example.finance.dto.response.SafeToSpendResponse;
import com.example.finance.entity.Budget;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.BudgetRepository;
import com.example.finance.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafeToSpendServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private SafeToSpendService safeToSpendService;

    @Test
    void calculateMonthlySafeToSpend_UsesVolatilityAndBudgetCommitments() {
        when(transactionRepository.getTotalByUserIdAndTypeAndDateRange(eq(1L), eq("INCOME"),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000.00"), new BigDecimal("3000.00"), new BigDecimal("2000.00"));

        when(budgetRepository.findActiveBudgetsByUserAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(Budget.builder().amount(new BigDecimal("500.00")).build()));

        when(accountRepository.getTotalBalanceByUserId(1L)).thenReturn(new BigDecimal("5000.00"));

        SafeToSpendResponse response = safeToSpendService.calculateMonthlySafeToSpend(1L, YearMonth.of(2026, 4));

        assertEquals(new BigDecimal("2000.00"), response.getBaselineIncome());
        assertEquals(new BigDecimal("500.00"), response.getBudgetCommitments());
        assertEquals(new BigDecimal("683.50"), response.getSafeToSpend());
    }

    @Test
    void calculateMonthlySafeToSpend_NeverReturnsNegativeValue() {
        when(transactionRepository.getTotalByUserIdAndTypeAndDateRange(eq(2L), eq("INCOME"),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(budgetRepository.findActiveBudgetsByUserAndDateRange(eq(2L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(Budget.builder().amount(new BigDecimal("100.00")).build()));

        when(accountRepository.getTotalBalanceByUserId(2L)).thenReturn(new BigDecimal("20.00"));

        SafeToSpendResponse response = safeToSpendService.calculateMonthlySafeToSpend(2L, YearMonth.of(2026, 4));

        assertEquals(BigDecimal.ZERO, response.getSafeToSpend());
    }
}
