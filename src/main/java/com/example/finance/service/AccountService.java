package com.example.finance.service;

import com.example.finance.dto.request.AccountRequest;
import com.example.finance.dto.response.AccountResponse;
import com.example.finance.entity.Account;
import com.example.finance.entity.User;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse createAccount(Long userId, AccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Account account = Account.builder().name(request.getName()).type(request.getType())
                .balance(request.getBalance()).accountNumber(request.getAccountNumber())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .description(request.getDescription()).user(user).build();

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long userId, Long accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateAccount(Long userId, Long accountId, AccountRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance());
        account.setAccountNumber(request.getAccountNumber());
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }
        account.setDescription(request.getDescription());

        Account updatedAccount = accountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));
        accountRepository.delete(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(Long userId) {
        return accountRepository.getTotalBalanceByUserId(userId);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder().id(account.getId()).name(account.getName()).type(account.getType())
                .balance(account.getBalance()).accountNumber(account.getAccountNumber()).currency(account.getCurrency())
                .description(account.getDescription()).isActive(account.getIsActive()).createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt()).build();
    }
}