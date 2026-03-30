package com.example.finance.service;

import com.example.finance.dto.request.AccountRequest;
import com.example.finance.dto.response.AccountResponse;
import com.example.finance.entity.Account;
import com.example.finance.entity.User;
import com.example.finance.exception.ResourceNotFoundException;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").email("test@example.com").password("password")
                .createdAt(LocalDateTime.now()).build();

        testAccount = Account.builder().id(1L).name("Test Account").type("BANK").balance(new BigDecimal("1000.00"))
                .currency("USD").isActive(true).user(testUser).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();

        accountRequest = AccountRequest.builder().name("Test Account").type("BANK").balance(new BigDecimal("1000.00"))
                .currency("USD").build();
    }

    @Test
    void createAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.createAccount(1L, accountRequest);

        assertNotNull(response);
        assertEquals("Test Account", response.getName());
        assertEquals("BANK", response.getType());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            accountService.createAccount(1L, accountRequest));
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.getAccountById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Account", response.getName());
    }

    @Test
    void getAccountById_NotFound() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            accountService.getAccountById(1L, 1L));
    }

    @Test
    void getAllAccounts_Success() {
        Account account2 = Account.builder().id(2L).name("Second Account").type("WALLET")
                .balance(new BigDecimal("500.00")).currency("USD").user(testUser).createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByUserId(1L)).thenReturn(Arrays.asList(testAccount, account2));

        List<AccountResponse> responses = accountService.getAllAccounts(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    void updateAccount_Success() {
        AccountRequest updateRequest = AccountRequest.builder().name("Updated Account").type("WALLET")
                .balance(new BigDecimal("2000.00")).currency("USD").build();

        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.updateAccount(1L, 1L, updateRequest);

        assertNotNull(response);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void deleteAccount_Success() {
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        doNothing().when(accountRepository).delete(any(Account.class));

        assertDoesNotThrow(() -> accountService.deleteAccount(1L, 1L));
        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    void getTotalBalance_Success() {
        when(accountRepository.getTotalBalanceByUserId(1L)).thenReturn(new BigDecimal("1500.00"));

        BigDecimal totalBalance = accountService.getTotalBalance(1L);

        assertEquals(new BigDecimal("1500.00"), totalBalance);
    }
}
