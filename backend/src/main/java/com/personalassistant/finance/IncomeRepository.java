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
public interface IncomeRepository extends JpaRepository<Income, String> {

    Page<Income> findByUserOrderByIncomeDateDesc(User user, Pageable pageable);

    List<Income> findByUserAndIncomeDateBetween(User user, Instant startDate, Instant endDate);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user")
    BigDecimal sumTotalByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.incomeDate >= :startDate AND i.incomeDate <= :endDate")
    BigDecimal sumTotalByUserAndDateBetween(@Param("user") User user, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
