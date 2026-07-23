package com.budgettracker.service;

import com.budgettracker.entity.MonthlyBudget;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;

import com.budgettracker.repository.MonthlyBudgetRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetService {

    private final MonthlyBudgetRepository budgetRepository;
    private final TransactionService transactionService;
    private final CurrentUserService currentUserService;

    public BudgetService(MonthlyBudgetRepository budgetRepository, TransactionService transactionService,
                        CurrentUserService currentUserService) {
        this.budgetRepository = budgetRepository;
        this.transactionService = transactionService;
        this.currentUserService = currentUserService;
    }

    public Optional<MonthlyBudget> getBudget(int month, int year) {
        return budgetRepository.findByUserAndMonthAndYear(currentUserService.getCurrentUser(), month, year);
    }

    public MonthlyBudget setOrUpdateBudget(int month, int year, Double limit) {
        MonthlyBudget budget = budgetRepository.findByUserAndMonthAndYear(currentUserService.getCurrentUser(), month, year)
                .orElse(new MonthlyBudget());
        budget.setMonth(month);
        budget.setYear(year);
        budget.setBudgetLimit(limit);
        budget.setUser(currentUserService.getCurrentUser());
        return budgetRepository.save(budget);
    }

    public Map<String, Object> getBudgetStatus(int month, int year) {
        Map<String, Object> status = new HashMap<>();
        User currentUser = currentUserService.getCurrentUser();
        Optional<MonthlyBudget> budgetOpt = budgetRepository.findByUserAndMonthAndYear(currentUser, month, year);

        double totalExpenses = transactionService.sumByMonthYearAndType(currentUser, month, year, TransactionType.EXPENSE);
        double totalIncome = transactionService.sumByMonthYearAndType(currentUser, month, year, TransactionType.INCOME);

        status.put("totalExpenses", totalExpenses);
        status.put("totalIncome", totalIncome);
        status.put("balance", totalIncome - totalExpenses);

        if (budgetOpt.isPresent()) {
            double limit = budgetOpt.get().getBudgetLimit();
            double percentage = limit > 0 ? (totalExpenses / limit) * 100 : 0;
            boolean exceeded = totalExpenses > limit;

            status.put("budgetLimit", limit);
            status.put("percentageUsed", Math.min(percentage, 100));
            status.put("exceeded", exceeded);
            status.put("remaining", Math.max(limit - totalExpenses, 0));
            status.put("hasBudget", true);

            if (exceeded) {
                status.put("alertMessage", String.format(
                    "Budget exceeded! You spent $%.2f over the $%.2f limit.", totalExpenses - limit, limit));
            } else if (percentage >= 80) {
                status.put("alertMessage", String.format(
                    "Warning: You've used %.1f%% of your monthly budget.", percentage));
            }
        } else {
            status.put("hasBudget", false);
            status.put("budgetLimit", 0);
            status.put("percentageUsed", 0);
            status.put("exceeded", false);
            status.put("remaining", 0);
        }

        return status;
    }

    public void deleteBudget(Long id) {
        MonthlyBudget existing = budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + id));
        ensureOwnedByCurrentUser(existing);
        budgetRepository.deleteById(id);
    }

    private void ensureOwnedByCurrentUser(MonthlyBudget budget) {
        User currentUser = currentUserService.getCurrentUser();
        if (budget.getUser() == null || !currentUser.getId().equals(budget.getUser().getId())) {
            throw new IllegalArgumentException("You do not have access to this budget");
        }
    }
}
