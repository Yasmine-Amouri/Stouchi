package com.budgettracker.service;

import com.budgettracker.dto.AuthResponse;
import com.budgettracker.dto.LoginRequest;
import com.budgettracker.dto.RegisterRequest;
import com.budgettracker.entity.Category;
import com.budgettracker.entity.TransactionType;
import com.budgettracker.entity.User;
import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        seedDefaultCategories(savedUser);
        return savedUser;
    }

    public void changePassword(String username, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getName(), user.getLastname());
    }

    private void seedDefaultCategories(User user) {
        List<Category> defaults = Arrays.asList(
                new Category("Salary", TransactionType.INCOME, "#22c55e", null, user),
                new Category("Freelance", TransactionType.INCOME, "#10b981", null, user),
                new Category("Investment", TransactionType.INCOME, "#3b82f6", null, user),
                new Category("Other Income", TransactionType.INCOME, "#8b5cf6", null, user),
                new Category("Food & Drinks", TransactionType.EXPENSE, "#ef4444", null, user),
                new Category("Transport", TransactionType.EXPENSE, "#f97316", null, user),
                new Category("Shopping", TransactionType.EXPENSE, "#ec4899", null, user),
                new Category("Housing", TransactionType.EXPENSE, "#6366f1", null, user),
                new Category("Healthcare", TransactionType.EXPENSE, "#14b8a6", null, user),
                new Category("Entertainment", TransactionType.EXPENSE, "#f59e0b", null, user),
                new Category("Education", TransactionType.EXPENSE, "#84cc16", null, user),
                new Category("Other", TransactionType.EXPENSE, "#94a3b8", null, user)
        );

        categoryRepository.saveAll(defaults);
    }
}