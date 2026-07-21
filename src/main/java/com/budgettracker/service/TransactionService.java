package com.budgettracker.service;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.TransactionRepository;
import com.budgettracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Transaction> getByMonthAndYear(int month, int year) {
        return transactionRepository.findByMonthAndYear(getCurrentUser(), month, year);
    }

    public List<Transaction> getByMonthYearAndType(int month, int year, TransactionType type) {
        return transactionRepository.findByMonthYearAndType(getCurrentUser(), month, year, type);
    }

    public Transaction create(Transaction transaction) {
        User currentUser = getCurrentUser();
        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(transaction.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            ensureOwnedByCurrentUser(cat);
            transaction.setCategory(cat);
        }
        transaction.setUser(currentUser);
        return transactionRepository.save(transaction);
    }

    public Transaction update(Long id, Transaction updated) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        ensureOwnedByCurrentUser(existing);
        existing.setDescription(updated.getDescription());
        existing.setAmount(updated.getAmount());
        existing.setType(updated.getType());
        existing.setDate(updated.getDate());
        existing.setNote(updated.getNote());
        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            ensureOwnedByCurrentUser(cat);
            existing.setCategory(cat);
        }
        return transactionRepository.save(existing);
    }

    public void delete(Long id) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        ensureOwnedByCurrentUser(existing);
        transactionRepository.deleteById(id);
    }

    public Double sumByMonthYearAndType(User user, int month, int year, TransactionType type) {
        Double result = transactionRepository.sumByMonthYearAndType(user, month, year, type);
        return result != null ? result : 0.0;
    }

    public List<Object[]> getExpensesByCategory(int month, int year) {
        return transactionRepository.sumExpensesByCategoryForMonth(getCurrentUser(), month, year);
    }

    private void ensureOwnedByCurrentUser(Transaction transaction) {
        User currentUser = getCurrentUser();
        if (transaction.getUser() == null || !currentUser.getId().equals(transaction.getUser().getId())) {
            throw new IllegalArgumentException("You do not have access to this transaction");
        }
    }

    private void ensureOwnedByCurrentUser(Category category) {
        User currentUser = getCurrentUser();
        if (category.getUser() == null || !currentUser.getId().equals(category.getUser().getId())) {
            throw new IllegalArgumentException("You do not have access to this category");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
