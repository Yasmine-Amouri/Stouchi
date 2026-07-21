package com.budgettracker.repository;

import com.budgettracker.entity.MonthlyBudget;
import com.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserAndMonthAndYear(User user, int month, int year);
}
