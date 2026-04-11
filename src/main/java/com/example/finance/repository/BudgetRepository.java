package com.example.finance.repository;

import com.example.finance.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    List<Budget> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.isActive = true " +
            "AND b.periodStart <= :endDate AND b.periodEnd >= :startDate")
    List<Budget> findActiveBudgetsByUserAndDateRange(@Param("userId")
    Long userId, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.category.id = :categoryId " +
            "AND b.isActive = true AND b.periodStart <= :endDate AND b.periodEnd >= :startDate")
    Optional<Budget> findByUserAndCategoryAndDateRange(@Param("userId")
    Long userId, @Param("categoryId")
    Long categoryId, @Param("startDate")
    LocalDate startDate, @Param("endDate")
    LocalDate endDate);

    boolean existsByCategoryIdAndUserIdAndIsActiveTrue(Long categoryId, Long userId);
}