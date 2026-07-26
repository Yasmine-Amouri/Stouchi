package com.budgettracker.service;

import com.budgettracker.repository.CategoryRepository;
import com.budgettracker.repository.UserRepository;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;

import com.budgettracker.dto.AuthResponse;
import com.budgettracker.dto.LoginRequest;

import com.budgettracker.entity.User;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

class UserServiceTest 
{

    @Test
    void login_whenNoUsernameExists_throwsException()
    {
        UserRepository fakeUserRepository = Mockito.mock(UserRepository.class);

        CategoryRepository fakeCategoryRepository = Mockito.mock(CategoryRepository.class);

        PasswordEncoder fakePasswordEncoder = Mockito.mock(PasswordEncoder.class);

        JwtService fakeJwtService = Mockito.mock(JwtService.class);

        UserService userService = new UserService(fakeUserRepository, fakeCategoryRepository, fakePasswordEncoder, fakeJwtService);

        LoginRequest request = new LoginRequest();
        request.setUsername("anais");
        
        when(fakeUserRepository.findByUsername("anais"))
            .thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.login(request));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(fakeUserRepository)
            .findByUsername("anais");

        verify(fakePasswordEncoder, never())
            .matches(anyString(), anyString());

        verify(fakeJwtService, never())
            .generateToken(anyString());
    }

    @Test
    void login_whenUsernameExistsPasswordWrong_throwsException()
    {
        UserRepository fakeUserRepository = Mockito.mock(UserRepository.class);

        CategoryRepository fakeCategoryRepository = Mockito.mock(CategoryRepository.class);

        PasswordEncoder fakePasswordEncoder = Mockito.mock(PasswordEncoder.class);

        JwtService fakeJwtService = Mockito.mock(JwtService.class);

        UserService userService = new UserService(fakeUserRepository, fakeCategoryRepository, fakePasswordEncoder, fakeJwtService);

        LoginRequest request = new LoginRequest();
        request.setUsername("anais");
        request.setPassword("meaw123");

        String hashedPassword = "hashed_pwd";

        User user = new User();
        user.setUsername("anais");
        user.setPassword(hashedPassword);
        
        when(fakeUserRepository.findByUsername("anais"))
            .thenReturn(Optional.of(user));

        when(fakePasswordEncoder.matches("meaw123", hashedPassword))
            .thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> userService.login(request));

        assertEquals("Invalid username or password", exception.getMessage());

        verify(fakeUserRepository)
            .findByUsername("anais");

        verify(fakePasswordEncoder)
            .matches("meaw123", "hashed_pwd");

        verify(fakeJwtService, never())
            .generateToken(anyString());
    }

    @Test
    void login_whenUsernameExistsAndPasswordCorrect_returnsAuthResponse()
    {
        UserRepository fakeUserRepository = Mockito.mock(UserRepository.class);

        CategoryRepository fakeCategoryRepository = Mockito.mock(CategoryRepository.class);

        PasswordEncoder fakePasswordEncoder = Mockito.mock(PasswordEncoder.class);

        JwtService fakeJwtService = Mockito.mock(JwtService.class);

        UserService userService = new UserService(fakeUserRepository, fakeCategoryRepository, fakePasswordEncoder, fakeJwtService);

        LoginRequest request = new LoginRequest();
        request.setUsername("anais");
        request.setPassword("meaw123");

        String hashedPassword = "hashed_pwd";

        User user = new User();
        user.setUsername("anais");
        user.setName("Anais");
        user.setLastname("Watterson");
        user.setPassword(hashedPassword);
        
        when(fakeUserRepository.findByUsername("anais"))
            .thenReturn(Optional.of(user));

        when(fakePasswordEncoder.matches("meaw123", hashedPassword))
            .thenReturn(true);

        when(fakeJwtService.generateToken("anais"))
            .thenReturn("token");
        
        AuthResponse response = userService.login(request);
        assertEquals("anais", response.getUsername());
        assertEquals("Anais", response.getName());
        assertEquals("Watterson", response.getLastname());
        assertEquals("token", response.getToken());

        verify(fakeUserRepository)
            .findByUsername("anais");

        verify(fakePasswordEncoder)
            .matches("meaw123", hashedPassword);

        verify(fakeJwtService)
            .generateToken("anais");
    }
}