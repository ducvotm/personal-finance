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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;
    private Transaction testTransaction;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").email("test@example.com").password("password")
                .createdAt(LocalDateTime.now()).build();

        testAccount = Account.builder().id(1L).name("Test Account").type("BANK").balance(new BigDecimal("1000.00"))
                .currency("USD").user(testUser).createdAt(LocalDateTime.now()).build();

        testCategory = Category.builder().id(1L).name("Food").type("EXPENSE").user(testUser)
                .createdAt(LocalDateTime.now()).build();

        testTransaction = Transaction.builder().id(1L).amount(new BigDecimal("50.00")).type("EXPENSE")
                .transactionDate(LocalDate.now()).description("Lunch").account(testAccount).category(testCategory)
                .user(testUser).createdAt(LocalDateTime.now()).build();

        transactionRequest = TransactionRequest.builder().amount(new BigDecimal("50.00")).type("EXPENSE")
                .transactionDate(LocalDate.now()).description("Lunch").accountId(1L).categoryId(1L).build();
    }

    @Test
    void createTransaction_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        TransactionResponse response = transactionService.createTransaction(1L, transactionRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals("EXPENSE", response.getType());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransaction_InsufficientBalance() {
        testAccount.setBalance(new BigDecimal("10.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCategory));

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(1L, transactionRequest));
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTransaction));

        TransactionResponse response = transactionService.getTransactionById(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            transactionService.getTransactionById(1L, 1L));
    }

    @Test
    void getAllTransactions_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction));

        when(transactionRepository.findByUserId(1L, pageable)).thenReturn(transactionPage);

        Page<TransactionResponse> responses = transactionService.getAllTransactions(1L, pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
    }

    @Test
    void getTransactionsByDateRange_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(transactionRepository.findByUserIdAndDateRange(1L, startDate, endDate))
                .thenReturn(Arrays.asList(testTransaction));

        List<TransactionResponse> responses = transactionService.getTransactionsByDateRange(1L, startDate, endDate);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTransaction));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        doNothing().when(transactionRepository).delete(any(Transaction.class));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L));
        verify(transactionRepository, times(1)).delete(testTransaction);
    }

    @Test
    void getTotalIncome_Success() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(transactionRepository.getTotalByUserIdAndTypeAndDateRange(1L, "INCOME", startDate, endDate))
                .thenReturn(new BigDecimal("5000.00"));

        BigDecimal totalIncome = transactionService.getTotalIncome(1L, startDate, endDate);

        assertEquals(new BigDecimal("5000.00"), totalIncome);
    }

    @Test
    void getTotalExpense_Success() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(transactionRepository.getTotalByUserIdAndTypeAndDateRange(1L, "EXPENSE", startDate, endDate))
                .thenReturn(new BigDecimal("2000.00"));

        BigDecimal totalExpense = transactionService.getTotalExpense(1L, startDate, endDate);

        assertEquals(new BigDecimal("2000.00"), totalExpense);
    }
}
