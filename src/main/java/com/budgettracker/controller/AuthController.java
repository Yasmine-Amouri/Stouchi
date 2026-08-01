package com.budgettracker.controller;

import com.budgettracker.dto.AuthResponse;
import com.budgettracker.dto.LoginRequest;
import com.budgettracker.dto.RegisterRequest;

import com.budgettracker.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        try
        {
            userService.register(request);
            return ResponseEntity.ok("User registered successfully");

        }
        catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        } 
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try 
        {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } 
        catch (BadCredentialsException e) 
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password");
        }
    }
}