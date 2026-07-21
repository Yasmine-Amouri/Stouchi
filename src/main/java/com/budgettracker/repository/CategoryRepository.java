package com.budgettracker.repository;

import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
    List<Category> findByUserAndType(User user, TransactionType type);
    boolean existsByUserAndName(User user, String name);
}
