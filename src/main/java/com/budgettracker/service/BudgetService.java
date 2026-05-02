package com.budgettracker.service;

import com.budgettracker.entity.MonthlyBudget;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.repository.MonthlyBudgetRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetService {

    private final MonthlyBudgetRepository budgetRepository;
    private final TransactionService transactionService;

    public BudgetService(MonthlyBudgetRepository budgetRepository, TransactionService transactionService) {
        this.budgetRepository = budgetRepository;
        this.transactionService = transactionService;
    }

    public Optional<MonthlyBudget> getBudget(int month, int year) {
        return budgetRepository.findByMonthAndYear(month, year);
    }

    public MonthlyBudget setOrUpdateBudget(int month, int year, Double limit) {
        MonthlyBudget budget = budgetRepository.findByMonthAndYear(month, year)
                .orElse(new MonthlyBudget());
        budget.setMonth(month);
        budget.setYear(year);
        budget.setBudgetLimit(limit);
        return budgetRepository.save(budget);
    }

    public Map<String, Object> getBudgetStatus(int month, int year) {
        Map<String, Object> status = new HashMap<>();
        Optional<MonthlyBudget> budgetOpt = budgetRepository.findByMonthAndYear(month, year);

        double totalExpenses = transactionService.sumByMonthYearAndType(month, year, TransactionType.EXPENSE);
        double totalIncome = transactionService.sumByMonthYearAndType(month, year, TransactionType.INCOME);

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
        budgetRepository.deleteById(id);
    }
}
