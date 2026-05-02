package com.budgettracker.service;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.Transaction;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Transaction> getByMonthAndYear(int month, int year) {
        return transactionRepository.findByMonthAndYear(month, year);
    }

    public List<Transaction> getByMonthYearAndType(int month, int year, TransactionType type) {
        return transactionRepository.findByMonthYearAndType(month, year, type);
    }

    public Transaction create(Transaction transaction) {
        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(transaction.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            transaction.setCategory(cat);
        }
        return transactionRepository.save(transaction);
    }

    public Transaction update(Long id, Transaction updated) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        existing.setDescription(updated.getDescription());
        existing.setAmount(updated.getAmount());
        existing.setType(updated.getType());
        existing.setDate(updated.getDate());
        existing.setNote(updated.getNote());
        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(updated.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existing.setCategory(cat);
        }
        return transactionRepository.save(existing);
    }

    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }

    public Double sumByMonthYearAndType(int month, int year, TransactionType type) {
        Double result = transactionRepository.sumByMonthYearAndType(month, year, type);
        return result != null ? result : 0.0;
    }

    public List<Object[]> getExpensesByCategory(int month, int year) {
        return transactionRepository.sumExpensesByCategoryForMonth(month, year);
    }
}
