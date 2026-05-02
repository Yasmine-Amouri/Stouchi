package com.budgettracker.service;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public List<Category> getByType(TransactionType type) {
        return categoryRepository.findByType(type);
    }

    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        if (category.getColor() == null || category.getColor().isBlank()) {
            category.setColor(randomColor());
        }
        if (category.getIcon() != null && category.getIcon().isBlank()) {
            category.setIcon(null);
        }
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category updated) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setColor(updated.getColor());
        existing.setIcon(updated.getIcon());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private String randomColor() {
        int value = ThreadLocalRandom.current().nextInt(0x1000000);
        return String.format("#%06x", value);
    }
}
