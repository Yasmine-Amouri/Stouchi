package com.budgettracker.config;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataLoader(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Salary", TransactionType.INCOME, "#22c55e", null));
            categoryRepository.save(new Category("Freelance", TransactionType.INCOME, "#10b981", null));
            categoryRepository.save(new Category("Investment", TransactionType.INCOME, "#3b82f6", null));
            categoryRepository.save(new Category("Other Income", TransactionType.INCOME, "#8b5cf6", null));

            categoryRepository.save(new Category("Food & Drinks", TransactionType.EXPENSE, "#ef4444", null));
            categoryRepository.save(new Category("Transport", TransactionType.EXPENSE, "#f97316", null));
            categoryRepository.save(new Category("Shopping", TransactionType.EXPENSE, "#ec4899", null));
            categoryRepository.save(new Category("Housing", TransactionType.EXPENSE, "#6366f1", null));
            categoryRepository.save(new Category("Healthcare", TransactionType.EXPENSE, "#14b8a6", null));
            categoryRepository.save(new Category("Entertainment", TransactionType.EXPENSE, "#f59e0b", null));
            categoryRepository.save(new Category("Education", TransactionType.EXPENSE, "#84cc16", null));
            categoryRepository.save(new Category("Other", TransactionType.EXPENSE, "#94a3b8", null));
        }
    }
}
