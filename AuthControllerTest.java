package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JockeyRepository jockeyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void getCurrentUser_whenNotAuthenticated_returns401() {

        ResponseEntity<?> response = authController.getCurrentUser(null);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void updateProfile_whenNotAuthenticated_returns401() {

        AuthController.UpdateProfileRequest request = new AuthController.UpdateProfileRequest();

        request.setFullName("Test User");

        ResponseEntity<?> response = authController.updateProfile(null, request);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void changePassword_whenNotAuthenticated_returns401() {

        AuthController.ChangePasswordRequest request = new AuthController.ChangePasswordRequest();

        request.setOldPassword("123456");
        request.setNewPassword("654321");

        ResponseEntity<?> response = authController.changePassword(null, request);

        assertEquals(401, response.getStatusCode().value());
    }
}