package com.budgettracker.service;

import com.budgettracker.repository.MonthlyBudgetRepository;

import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.entity.MonthlyBudget;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

class BudgetServiceTest 
{

    @Test
    void getBudgetStatus_whenNoBudgetExists_returnsStatusWithoutBudget()
    {
        MonthlyBudgetRepository fakeBudgetRepository = Mockito.mock(MonthlyBudgetRepository.class);

        TransactionService fakeTransactionService = Mockito.mock(TransactionService.class);

        CurrentUserService fakeCurrentUserService = Mockito.mock(CurrentUserService.class);

        BudgetService budgetService = new BudgetService(fakeBudgetRepository, fakeTransactionService, fakeCurrentUserService);

        User user = new User();
        user.setUsername("anais");
        when(fakeCurrentUserService.getCurrentUser())
            .thenReturn(user);
        
        when(fakeBudgetRepository.findByUserAndMonthAndYear(user, 7, 2026))
            .thenReturn(Optional.empty());
        
        when(fakeTransactionService.sumByMonthYearAndType(user, 7, 2026, TransactionType.EXPENSE))
            .thenReturn(400.0);
        when(fakeTransactionService.sumByMonthYearAndType(user, 7, 2026, TransactionType.INCOME))
            .thenReturn(1200.0);
        
        Map<String,Object> result = budgetService.getBudgetStatus(7, 2026);

        assertEquals(400.0, result.get("totalExpenses"));
        assertEquals(1200.0, result.get("totalIncome"));
        assertEquals(800.0, result.get("balance"));

        assertEquals(false, result.get("hasBudget"));
        assertEquals(0, result.get("budgetLimit"));
        assertEquals(0, result.get("percentageUsed"));
        assertEquals(false, result.get("exceeded"));
        assertEquals(0, result.get("remaining"));

    }

    @Test
    void getBudgetStatus_whenUsageBelow80Percent_hasNoAlert()
    {
        MonthlyBudgetRepository fakeBudgetRepository = Mockito.mock(MonthlyBudgetRepository.class);

        TransactionService fakeTransactionService = Mockito.mock(TransactionService.class);

        CurrentUserService fakeCurrentUserService = Mockito.mock(CurrentUserService.class);

        BudgetService budgetService = new BudgetService(fakeBudgetRepository, fakeTransactionService, fakeCurrentUserService);

        User user = new User();
        user.setUsername("anais");
        when(fakeCurrentUserService.getCurrentUser())
            .thenReturn(user);
        
        MonthlyBudget monthlyBudget = new MonthlyBudget();
        monthlyBudget.setBudgetLimit(1000.0);
        when(fakeBudgetRepository.findByUserAndMonthAndYear(user, 6, 2026))
            .thenReturn(Optional.of(monthlyBudget));
        
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.EXPENSE))
            .thenReturn(200.0);
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.INCOME))
            .thenReturn(100.0);
        
        Map<String,Object> result = budgetService.getBudgetStatus(6, 2026);

        assertEquals(200.0, result.get("totalExpenses"));
        assertEquals(100.0, result.get("totalIncome"));
        assertEquals(-100.0, result.get("balance"));

        assertEquals(true, result.get("hasBudget"));
        assertEquals(1000.0, result.get("budgetLimit"));
        assertEquals(20.0, result.get("percentageUsed"));
        assertEquals(false, result.get("exceeded"));
        assertEquals(800.0, result.get("remaining"));

        assertFalse(result.containsKey("alertMessage"));

    }

    @Test
    void getBudgetStatus_whenUsageReaches100Percent_warningAlert()
    {
        MonthlyBudgetRepository fakeBudgetRepository = Mockito.mock(MonthlyBudgetRepository.class);

        TransactionService fakeTransactionService = Mockito.mock(TransactionService.class);

        CurrentUserService fakeCurrentUserService = Mockito.mock(CurrentUserService.class);

        BudgetService budgetService = new BudgetService(fakeBudgetRepository, fakeTransactionService, fakeCurrentUserService);

        User user = new User();
        user.setUsername("anais");
        when(fakeCurrentUserService.getCurrentUser())
            .thenReturn(user);
        
        MonthlyBudget monthlyBudget = new MonthlyBudget();
        monthlyBudget.setBudgetLimit(1000.0);
        when(fakeBudgetRepository.findByUserAndMonthAndYear(user, 6, 2026))
            .thenReturn(Optional.of(monthlyBudget));
        
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.EXPENSE))
            .thenReturn(1000.0);
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.INCOME))
            .thenReturn(100.0);
        
        Map<String,Object> result = budgetService.getBudgetStatus(6, 2026);

        assertEquals(1000.0, result.get("totalExpenses"));
        assertEquals(100.0, result.get("totalIncome"));
        assertEquals(-900.0, result.get("balance"));

        assertEquals(true, result.get("hasBudget"));
        assertEquals(1000.0, result.get("budgetLimit"));
        assertEquals(100.0, result.get("percentageUsed"));
        assertEquals(false, result.get("exceeded"));
        assertEquals(0.0, result.get("remaining"));

        assertTrue(result.containsKey("alertMessage"));
        assertEquals("Warning: You've used 100.0% of your monthly budget.", result.get("alertMessage"));

    }

    @Test
    void getBudgetStatus_whenUsageOver100Percent_exceededAlert()
    {
        MonthlyBudgetRepository fakeBudgetRepository = Mockito.mock(MonthlyBudgetRepository.class);

        TransactionService fakeTransactionService = Mockito.mock(TransactionService.class);

        CurrentUserService fakeCurrentUserService = Mockito.mock(CurrentUserService.class);

        BudgetService budgetService = new BudgetService(fakeBudgetRepository, fakeTransactionService, fakeCurrentUserService);

        User user = new User();
        user.setUsername("anais");
        when(fakeCurrentUserService.getCurrentUser())
            .thenReturn(user);
        
        MonthlyBudget monthlyBudget = new MonthlyBudget();
        monthlyBudget.setBudgetLimit(1000.0);
        when(fakeBudgetRepository.findByUserAndMonthAndYear(user, 6, 2026))
            .thenReturn(Optional.of(monthlyBudget));
        
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.EXPENSE))
            .thenReturn(1100.0);
        when(fakeTransactionService.sumByMonthYearAndType(user, 6, 2026, TransactionType.INCOME))
            .thenReturn(100.0);
        
        Map<String,Object> result = budgetService.getBudgetStatus(6, 2026);

        assertEquals(1100.0, result.get("totalExpenses"));
        assertEquals(100.0, result.get("totalIncome"));
        assertEquals(-1000.0, result.get("balance"));

        assertEquals(true, result.get("hasBudget"));
        assertEquals(1000.0, result.get("budgetLimit"));
        assertEquals(100.0, result.get("percentageUsed"));
        assertEquals(true, result.get("exceeded"));
        assertEquals(0.0, result.get("remaining"));

        assertTrue(result.containsKey("alertMessage"));
        //if I write in wrong format: 100 instead of 100.00 the test will fail
        assertEquals("Budget exceeded! You spent $100.00 over the $1000.00 limit.", result.get("alertMessage"));
    }
}