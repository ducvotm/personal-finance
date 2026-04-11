package com.example.finance.repository;

import com.example.finance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = {"account", "category"})
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"account", "category"})
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"account", "category"})
    List<Transaction> findByAccountId(Long accountId);

    @EntityGraph(attributePaths = {"account", "category"})
    List<Transaction> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"account", "category"})
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateRange(@Param("userId")
    Long userId, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalByUserIdAndTypeAndDateRange(@Param("userId")
    Long userId, @Param("type")
    String type, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalByUserIdAndCategoryIdAndTypeAndDateRange(@Param("userId")
    Long userId, @Param("categoryId")
    Long categoryId, @Param("type")
    String type, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);

    @EntityGraph(attributePaths = {"account", "category"})
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndAccountIdAndDateRange(@Param("userId")
    Long userId, @Param("accountId")
    Long accountId, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);
}
