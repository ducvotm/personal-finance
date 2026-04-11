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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (budgetRepository.existsByCategoryIdAndUserIdAndIsActiveTrue(request.getCategoryId(), userId)) {
            throw new BadRequestException(
                    "Budget already exists for this category. Please update the existing budget.");
        }

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BadRequestException("End date must be after start date");
        }

        Budget budget = Budget.builder().amount(request.getAmount()).periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd()).periodType(request.getPeriodType()).category(category).user(user)
                .isActive(true).spentAmount(BigDecimal.ZERO).build();

        Budget savedBudget = budgetRepository.save(budget);
        return mapToResponse(savedBudget);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets(Long userId) {
        return budgetRepository.findByUserIdAndIsActiveTrue(userId).stream().map(this::mapToResponseWithSpent)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        return mapToResponseWithSpent(budget);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return budgetRepository.findActiveBudgetsByUserAndDateRange(userId, startDate, endDate).stream()
                .map(this::mapToResponseWithSpent).collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BadRequestException("End date must be after start date");
        }

        budget.setAmount(request.getAmount());
        budget.setPeriodStart(request.getPeriodStart());
        budget.setPeriodEnd(request.getPeriodEnd());
        budget.setPeriodType(request.getPeriodType());
        budget.setCategory(category);

        Budget savedBudget = budgetRepository.save(budget);
        return mapToResponseWithSpent(savedBudget);
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        budget.setIsActive(false);
        budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateSpentAmount(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.getTotalByUserIdAndCategoryIdAndTypeAndDateRange(userId, categoryId,
                "EXPENSE", startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    private BudgetResponse mapToResponseWithSpent(Budget budget) {
        BigDecimal spent = calculateSpentAmount(budget.getUser().getId(), budget.getCategory().getId(),
                budget.getPeriodStart(), budget.getPeriodEnd());

        BigDecimal remaining = budget.getAmount().subtract(spent);
        double percentUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                        .doubleValue()
                : 0.0;

        return BudgetResponse.builder().id(budget.getId()).amount(budget.getAmount())
                .periodStart(budget.getPeriodStart()).periodEnd(budget.getPeriodEnd())
                .periodType(budget.getPeriodType()).spentAmount(spent).remainingAmount(remaining)
                .percentUsed(percentUsed).isActive(budget.getIsActive()).createdAt(budget.getCreatedAt())
                .categoryId(budget.getCategory().getId()).categoryName(budget.getCategory().getName())
                .categoryIcon(budget.getCategory().getIcon()).categoryColor(budget.getCategory().getColor()).build();
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder().id(budget.getId()).amount(budget.getAmount())
                .periodStart(budget.getPeriodStart()).periodEnd(budget.getPeriodEnd())
                .periodType(budget.getPeriodType()).spentAmount(budget.getSpentAmount()).isActive(budget.getIsActive())
                .createdAt(budget.getCreatedAt()).categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName()).categoryIcon(budget.getCategory().getIcon())
                .categoryColor(budget.getCategory().getColor()).build();
    }
}