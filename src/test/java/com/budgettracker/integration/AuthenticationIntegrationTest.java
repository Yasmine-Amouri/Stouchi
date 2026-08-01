package com.budgettracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.junit.jupiter.api.BeforeEach;
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

import com.budgettracker.service.JwtService;

import com.budgettracker.entity.User;

import com.budgettracker.dto.AuthResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    //one container per class not per method (speed over isolation)
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @BeforeEach
    void cleanDatabase()
    {
        userRepository.deleteAll();
    }

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

    @Test
    void shouldNotRegisterDuplicateUser() throws Exception
    {
        User user = new User();
        user.setUsername("anais");
        userRepository.save(user);

        String requestBody = """
        {
            "name": "Anais",
            "lastname": "Watterson",
            "username": "anais",
            "password": "meaw123"
        }
        """;

        ResultActions res = mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        //The actual HTTP status is 409 ?
        res.andExpect(MockMvcResultMatchers.status().isConflict());

        res.andExpect(MockMvcResultMatchers.content().string("Username already exists"));

        //Verify no insertion in db
        assertEquals(1, userRepository.count());
    }

    @Test
    void shouldNotLoginUsernameDoesntExist() throws Exception
    {
        String requestBody = """
        {
            "username": "anais",
            "password": "meaw123"
        }
        """;

        ResultActions res = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        //The actual HTTP status is 401 ?
        res.andExpect(MockMvcResultMatchers.status().isUnauthorized());

        res.andExpect(MockMvcResultMatchers.content().string("Invalid username or password"));
    }

    @Test
    void shouldNotLoginUsernameExistsPasswordWrong() throws Exception
    {
        User user = new User();
        user.setUsername("anais");
        user.setPassword(passwordEncoder.encode("meaw123"));
        userRepository.save(user);

        String requestBody = """
        {
            "username": "anais",
            "password": "meaw12"
        }
        """;

        ResultActions res = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        //The actual HTTP status is 401 ?
        res.andExpect(MockMvcResultMatchers.status().isUnauthorized());

        res.andExpect(MockMvcResultMatchers.content().string("Invalid username or password"));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception
    {
        User user = new User();
        user.setUsername("anais");
        user.setName("Anais");
        user.setLastname("Watterson");
        user.setPassword(passwordEncoder.encode("meaw123"));
        
        userRepository.save(user);

        String requestBody = """
        {
            "username": "anais",
            "password": "meaw123"
        }
        """;

        ResultActions res = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        //The actual HTTP status is 200 ?
        res.andExpect(MockMvcResultMatchers.status().isOk());

        res.andExpect(MockMvcResultMatchers.jsonPath("$.username").value("anais"));
        res.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Anais"));
        res.andExpect(MockMvcResultMatchers.jsonPath("$.lastname").value("Watterson"));

        String token = objectMapper.readValue(res.andReturn().getResponse().getContentAsString(), AuthResponse.class).getToken();
        assertTrue(jwtService.validateToken(token));
    }
}