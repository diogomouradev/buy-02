package com.gritlab.buy01.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gritlab.buy01.userservice.model.enums.Role;
import com.gritlab.buy01.userservice.payload.request.LoginRequest;
import com.gritlab.buy01.userservice.payload.request.SignupRequest;
import com.gritlab.buy01.userservice.repository.UserRepository;
import com.gritlab.buy01.userservice.security.UserDetailsImpl;
import com.gritlab.buy01.userservice.security.jwt.JwtUtils;

public class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder encoder;

  @Mock private JwtUtils jwtUtils;

  @Mock private Authentication authentication;

  @InjectMocks private AuthService authService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testAuthenticateUser() {
    LoginRequest request = new LoginRequest();
    request.setName("testName");
    request.setPassword("testPassword");

    UserDetailsImpl userDetails =
        new UserDetailsImpl(
            "1", "testName", "test@email.com", "testPassword", Collections.emptyList());
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(jwtUtils.generateTokenFromUserId("1")).thenReturn("testToken");

    var response = authService.authenticateUser(request);

    assertNotNull(response);
    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void testRegisterUser() {
    SignupRequest request = new SignupRequest();
    request.setName("testName");
    request.setEmail("test@email.com");
    request.setPassword("testPassword");
    request.setRole(Role.CLIENT);

    when(userRepository.existsByName("testName")).thenReturn(false);
    when(userRepository.existsByEmail("test@email.com")).thenReturn(false);

    ResponseEntity response = authService.registerUser(request);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }
}
