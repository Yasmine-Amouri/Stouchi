package com.budgettracker.repository;

import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND MONTH(t.date) = :month AND YEAR(t.date) = :year ORDER BY t.date DESC")
    List<Transaction> findByMonthAndYear(@Param("user") User user, @Param("month") int month, @Param("year") int year);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND MONTH(t.date) = :month AND YEAR(t.date) = :year AND t.type = :type ORDER BY t.date DESC")
    List<Transaction> findByMonthYearAndType(@Param("user") User user, @Param("month") int month, @Param("year") int year, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND MONTH(t.date) = :month AND YEAR(t.date) = :year AND t.type = :type")
    Double sumByMonthYearAndType(@Param("user") User user, @Param("month") int month, @Param("year") int year, @Param("type") TransactionType type);

    @Query("SELECT t.category.name, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND MONTH(t.date) = :month AND YEAR(t.date) = :year AND t.type = 'EXPENSE' GROUP BY t.category.name")
    List<Object[]> sumExpensesByCategoryForMonth(@Param("user") User user, @Param("month") int month, @Param("year") int year);
}
