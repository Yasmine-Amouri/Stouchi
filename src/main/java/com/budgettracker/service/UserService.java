package com.budgettracker.service;

import com.budgettracker.entity.User;
import com.budgettracker.repository.UserRepository;

import com.budgettracker.dto.AuthResponse;
import com.budgettracker.dto.LoginRequest;
import com.budgettracker.dto.RegisterRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;


@Service
public class UserService 
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());

        String hashedPassword = passwordEncoder.encode(
                request.getPassword()
        );

        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public void changePassword(String username, String newPassword) 
    {

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) 
        {
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(
                request.getUsername()
        )
        .orElseThrow(() ->
                new BadCredentialsException("Invalid username or password")
        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(
                user.getUsername()
        );

        return new AuthResponse(token);
    }
}