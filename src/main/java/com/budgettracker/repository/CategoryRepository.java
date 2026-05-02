package com.budgettracker.repository;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(TransactionType type);
    boolean existsByName(String name);
}
