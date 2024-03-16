package com.gritlab.buy01.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.model.enums.Role;
import com.gritlab.buy01.userservice.payload.request.LoginRequest;
import com.gritlab.buy01.userservice.payload.request.SignupRequest;
import com.gritlab.buy01.userservice.service.AuthService;

public class AuthControllerTest {

  private AuthController authController;
  private AuthService authServiceMock;

  @BeforeEach
  public void setUp() {
    authServiceMock = mock(AuthService.class);
    authController = new AuthController();
    authController.authService = authServiceMock;
  }

  @Test
  public void testAuthenticateUser() {
    LoginRequest request = new LoginRequest();
    ResponseEntity expectedResponse = ResponseEntity.ok().body("Success");

    when(authServiceMock.authenticateUser(any())).thenReturn(expectedResponse);

    ResponseEntity<?> actualResponse = authController.authenticateUser(request);

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testRegisterUser() {
    SignupRequest request = new SignupRequest();
    ResponseEntity expectedResponse = ResponseEntity.ok().body("User Registered");

    when(authServiceMock.registerUser(request)).thenReturn(expectedResponse);

    ResponseEntity<?> actualResponse = authController.registerUser(request);

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  public void testCheckAuth_withCookie() {
    String cookieValue = "someValue";
    User user = new User("name", "email@email.com", "password", Role.CLIENT);
    when(authServiceMock.checkAuth(cookieValue)).thenReturn(user);

    ResponseEntity<?> response = authController.checkAuth(cookieValue);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(user, response.getBody());
  }

  @Test
  public void testCheckAuth_withoutCookie() {
    ResponseEntity<?> response = authController.checkAuth(null);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  public void testSignOut_withToken() {
    String token = "someToken";
    ResponseEntity<HttpStatus> response = authController.signOut(token);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testSignOut_withoutToken() {
    ResponseEntity<HttpStatus> response = authController.signOut(null);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }
}
