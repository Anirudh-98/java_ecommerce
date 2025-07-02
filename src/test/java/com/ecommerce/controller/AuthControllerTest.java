package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.payload.request.LoginRequest;
import com.ecommerce.payload.request.SignupRequest;
import com.ecommerce.payload.response.JwtResponse;
import com.ecommerce.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Import necessary classes from your project that are missing
import com.ecommerce.security.JwtAuthenticationEntryPoint;
import com.ecommerce.security.JwtRequestFilter;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.UserDetailsServiceImpl;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // Add Mocks for Spring Security components that AuthController might depend on implicitly
    // or through the test context loading Spring Security configurations.
    @MockBean
    private UserDetailsServiceImpl userDetailsService; // If your SecurityConfig uses it

    @MockBean
    private JwtUtil jwtUtil; // If your JwtRequestFilter or other security components use it

    @MockBean
    private JwtRequestFilter jwtRequestFilter; // To avoid issues with filter chain in tests

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // If used in security config

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        jwtResponse = new JwtResponse("test.jwt.token", "testuser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void registerUser_success() throws Exception {
        when(authService.registerUser(any(SignupRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully! User ID: 1"));
    }

    @Test
    void registerUser_usernameTaken() throws Exception {
        when(authService.registerUser(any(SignupRequest.class)))
                .thenThrow(new RuntimeException("Error: Username is already taken!"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void authenticateUser_success() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

     @Test
    void authenticateUser_invalidCredentials() throws Exception {
        when(authService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Or whatever your global exception handler returns for BadCredentialsException
    }
}
