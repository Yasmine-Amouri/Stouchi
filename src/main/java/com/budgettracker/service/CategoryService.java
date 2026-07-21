package com.budgettracker.service;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findByUser(getCurrentUser());
    }

    public List<Category> getByType(TransactionType type) {
        return categoryRepository.findByUserAndType(getCurrentUser(), type);
    }

    public Category create(Category category) {
        User currentUser = getCurrentUser();
        if (categoryRepository.existsByUserAndName(currentUser, category.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        if (category.getColor() == null || category.getColor().isBlank()) {
            category.setColor(randomColor());
        }
        if (category.getIcon() != null && category.getIcon().isBlank()) {
            category.setIcon(null);
        }
        category.setUser(currentUser);
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category updated) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        ensureOwnedByCurrentUser(existing);
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setColor(updated.getColor());
        existing.setIcon(updated.getIcon());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        ensureOwnedByCurrentUser(existing);
        categoryRepository.deleteById(id);
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

    private String randomColor() {
        int value = ThreadLocalRandom.current().nextInt(0x1000000);
        return String.format("#%06x", value);
    }
}
