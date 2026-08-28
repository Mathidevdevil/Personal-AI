package com.personalassistant.finance;

import com.personalassistant.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {

    Page<Expense> findByUserOrderByTransactionDateDesc(User user, Pageable pageable);

    Page<Expense> findByUserAndCategoryOrderByTransactionDateDesc(User user, ExpenseCategory category, Pageable pageable);

    Page<Expense> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
            User user, Instant startDate, Instant endDate, Pageable pageable
    );

    List<Expense> findByUserAndTransactionDateBetween(User user, Instant startDate, Instant endDate);

    List<Expense> findTop5ByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.transactionDate >= :startDate AND e.transactionDate <= :endDate")
    BigDecimal sumTotalByUserAndDateBetween(@Param("user") User user, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category = :category AND e.transactionDate >= :startDate AND e.transactionDate <= :endDate")
    BigDecimal sumByUserAndCategoryAndDateBetween(@Param("user") User user, @Param("category") ExpenseCategory category, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
