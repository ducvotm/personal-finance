package com.example.finance.service;

import com.example.finance.entity.Account;
import com.example.finance.entity.Category;
import com.example.finance.entity.User;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDataService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    private static final List<String> DEFAULT_EXPENSE_CATEGORIES = List.of("Food", "Transport", "Shopping",
            "Entertainment", "Healthcare", "Utilities", "Other");

    private static final List<String> DEFAULT_INCOME_CATEGORIES = List.of("Salary", "Investment", "Business", "Gift",
            "Other");

    @Transactional
    public void createDefaultData(User user) {
        log.info("Creating default data for user: {}", user.getUsername());

        createDefaultAccount(user);
        createDefaultCategories(user);

        log.info("Default data created successfully for user: {}", user.getUsername());
    }

    private void createDefaultAccount(User user) {
        Account account = Account.builder().name("Cash").type("CASH").balance(BigDecimal.ZERO).currency("USD")
                .description("Default cash account").isActive(true).user(user).build();

        accountRepository.save(account);
        log.info("Created default account: Cash for user: {}", user.getUsername());
    }

    private void createDefaultCategories(User user) {
        for (String categoryName : DEFAULT_EXPENSE_CATEGORIES) {
            Category category = Category.builder().name(categoryName).type("EXPENSE")
                    .icon(getIconForCategory(categoryName)).color(getColorForCategory(categoryName)).user(user).build();
            categoryRepository.save(category);
        }

        for (String categoryName : DEFAULT_INCOME_CATEGORIES) {
            Category category = Category.builder().name(categoryName).type("INCOME")
                    .icon(getIconForCategory(categoryName)).color(getColorForCategory(categoryName)).user(user).build();
            categoryRepository.save(category);
        }

        log.info("Created {} expense and {} income categories for user: {}", DEFAULT_EXPENSE_CATEGORIES.size(),
                DEFAULT_INCOME_CATEGORIES.size(), user.getUsername());
    }

    private String getIconForCategory(String name) {
        return switch (name.toLowerCase()) {
            case "food" -> "🍔";
            case "transport" -> "🚗";
            case "shopping" -> "🛒";
            case "entertainment" -> "🎬";
            case "healthcare" -> "🏥";
            case "utilities" -> "💡";
            case "salary" -> "💰";
            case "investment" -> "📈";
            case "business" -> "💼";
            case "gift" -> "🎁";
            default -> "📁";
        };
    }

    private String getColorForCategory(String name) {
        return switch (name.toLowerCase()) {
            case "food" -> "#FF6B6B";
            case "transport" -> "#4ECDC4";
            case "shopping" -> "#45B7D1";
            case "entertainment" -> "#96CEB4";
            case "healthcare" -> "#FFEAA7";
            case "utilities" -> "#DDA0DD";
            case "salary" -> "#98D8C8";
            case "investment" -> "#F7DC6F";
            case "business" -> "#BB8FCE";
            case "gift" -> "#F1948A";
            default -> "#95A5A6";
        };
    }
}