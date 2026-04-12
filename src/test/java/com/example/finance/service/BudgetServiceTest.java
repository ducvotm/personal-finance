package com.example.finance.service;

import com.example.finance.dto.request.BudgetRequest;
import com.example.finance.dto.response.BudgetResponse;
import com.example.finance.entity.Budget;
import com.example.finance.entity.Category;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.BudgetRepository;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Category testCategory;
    private Budget testBudget;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("u").email("u@x.com").password("p").role("USER")
                .createdAt(LocalDateTime.now()).build();

        testCategory = Category.builder().id(10L).name("Food").type("EXPENSE").icon("").color("#000").user(testUser)
                .createdAt(LocalDateTime.now()).build();

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        testBudget = Budget.builder().id(5L).amount(new BigDecimal("500.00")).periodStart(start).periodEnd(end)
                .periodType("MONTHLY").spentAmount(BigDecimal.ZERO).isActive(true).category(testCategory).user(testUser)
                .createdAt(LocalDateTime.now()).build();

        budgetRequest = BudgetRequest.builder().amount(new BigDecimal("500.00")).periodStart(start).periodEnd(end)
                .periodType("MONTHLY").categoryId(10L).build();
    }

    @Test
    void createBudget_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByCategoryIdAndUserIdAndIsActiveTrue(10L, 1L)).thenReturn(false);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        BudgetResponse response = budgetService.createBudget(1L, budgetRequest);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void createBudget_DuplicateActiveCategory() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByCategoryIdAndUserIdAndIsActiveTrue(10L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> budgetService.createBudget(1L, budgetRequest));
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void createBudget_InvalidPeriod() {
        budgetRequest.setPeriodStart(LocalDate.of(2026, 2, 1));
        budgetRequest.setPeriodEnd(LocalDate.of(2026, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(testCategory));
        when(budgetRepository.existsByCategoryIdAndUserIdAndIsActiveTrue(10L, 1L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> budgetService.createBudget(1L, budgetRequest));
    }

    @Test
    void getBudgetById_IncludesSpent() {
        when(budgetRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(testBudget));
        when(transactionRepository.getTotalByUserIdAndCategoryIdAndTypeAndDateRange(eq(1L), eq(10L), eq("EXPENSE"),
                any(LocalDate.class), any(LocalDate.class))).thenReturn(new BigDecimal("100.00"));

        BudgetResponse response = budgetService.getBudgetById(1L, 5L);

        assertEquals(new BigDecimal("100.00"), response.getSpentAmount());
        assertEquals(new BigDecimal("400.00"), response.getRemainingAmount());
    }

    @Test
    void deleteBudget_SoftDeactivates() {
        when(budgetRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        budgetService.deleteBudget(1L, 5L);

        assertFalse(testBudget.getIsActive());
        verify(budgetRepository).save(testBudget);
    }

    @Test
    void getBudgetById_NotFound() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.getBudgetById(1L, 99L));
    }
}
