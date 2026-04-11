package com.example.finance.service;

import com.example.finance.dto.request.TransactionRequest;
import com.example.finance.dto.response.TransactionResponse;
import com.example.finance.entity.Account;
import com.example.finance.entity.Category;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import com.example.finance.exception.BadRequestException;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.CategoryRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getAccountId()));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Transaction transaction = Transaction.builder().amount(request.getAmount()).type(request.getType())
                .transactionDate(request.getTransactionDate()).description(request.getDescription())
                .note(request.getNote())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurringFrequency(request.getRecurringFrequency()).account(account).category(category).user(user)
                .build();

        updateAccountBalance(account, request.getAmount(), request.getType());

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        reverseAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType());

        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getAccountId()));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setNote(request.getNote());
        transaction.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        transaction.setRecurringFrequency(request.getRecurringFrequency());
        transaction.setAccount(account);
        transaction.setCategory(category);

        updateAccountBalance(account, request.getAmount(), request.getType());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return mapToResponse(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        reverseAccountBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType());
        accountRepository.save(transaction.getAccount());

        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalByUserIdAndTypeAndDateRange(userId, "INCOME", startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpense(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getTotalByUserIdAndTypeAndDateRange(userId, "EXPENSE", startDate, endDate);
    }

    private void updateAccountBalance(Account account, BigDecimal amount, String type) {
        if ("INCOME".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance().add(amount));
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient balance in account");
            }
            account.setBalance(account.getBalance().subtract(amount));
        }
        accountRepository.save(account);
    }

    private void reverseAccountBalance(Account account, BigDecimal amount, String type) {
        if ("INCOME".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder().id(transaction.getId()).amount(transaction.getAmount())
                .type(transaction.getType()).transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription()).note(transaction.getNote())
                .isRecurring(transaction.getIsRecurring()).recurringFrequency(transaction.getRecurringFrequency())
                .createdAt(transaction.getCreatedAt()).updatedAt(transaction.getUpdatedAt())
                .accountId(transaction.getAccount().getId()).accountName(transaction.getAccount().getName())
                .categoryId(transaction.getCategory().getId()).categoryName(transaction.getCategory().getName())
                .build();
    }
}
