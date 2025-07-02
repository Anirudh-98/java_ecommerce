package com.ecommerce.service;

import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.payload.request.LoginRequest;
import com.ecommerce.payload.request.SignupRequest;
import com.ecommerce.payload.response.JwtResponse;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("password");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);
    }

    @Test
    void registerUser_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = authService.registerUser(signupRequest);

        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertTrue(registeredUser.getRoles().contains(Role.USER));
        verify(userRepository, times(1)).save(any(User.class));
        verify(wishlistRepository, times(1)).save(any());
        verify(cartRepository, times(1)).save(any());
    }

    @Test
    void registerUser_usernameTaken() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.registerUser(signupRequest);
        });

        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_success() {
        Authentication authentication = mock(Authentication.class);
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "testuser",
                        "encodedPassword",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password")))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("test.jwt.token");

        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);

        assertNotNull(jwtResponse);
        assertEquals("test.jwt.token", jwtResponse.getToken());
        assertEquals("testuser", jwtResponse.getUsername());
        assertTrue(jwtResponse.getRoles().contains("ROLE_USER"));
    }
}
