package com.budgettracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.budgettracker.repository.UserRepository;

import com.budgettracker.entity.User;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthenticationIntegrationTest 
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //one container per class not per method (speed over isolation)
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test   
    void shouldRegisterUserSuccessfully() throws Exception
    {
        String requestBody = """
        {
            "name": "Anais",
            "lastname": "Watterson",
            "username": "anais",
            "password": "meaw123"
        }
        """;

        //chaining: the param is of type MockHttpServletRequestBuilder
        ResultActions res = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        //The actual HTTP status is 200 ?
        res.andExpect(MockMvcResultMatchers.status().isOk());

        //check also the db
        Optional<User> user = userRepository.findByUsername("anais");

        //user with username "anais" exists?
        assertTrue(user.isPresent());

        User savedUser = user.get();

        assertEquals("anais", savedUser.getUsername());
        assertEquals("Anais", savedUser.getName());
        assertEquals("Watterson", savedUser.getLastname());
        assertTrue(passwordEncoder.matches("meaw123", savedUser.getPassword()));
    }
}