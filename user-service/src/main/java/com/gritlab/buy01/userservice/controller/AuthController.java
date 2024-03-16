package com.gritlab.buy01.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.payload.request.LoginRequest;
import com.gritlab.buy01.userservice.payload.request.SignupRequest;
import com.gritlab.buy01.userservice.service.AuthService;

import jakarta.validation.Valid;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired AuthService authService;

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    return authService.authenticateUser(loginRequest);
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
    return authService.registerUser(signupRequest);
  }

  @GetMapping("/check")
  public ResponseEntity<?> checkAuth(
      @CookieValue(name = "buy-01", required = false) String cookie) {
    if (cookie == null) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    User user = authService.checkAuth(cookie);
    if (user != null) return new ResponseEntity<>(user, HttpStatus.OK);
    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
  }

  @PostMapping("/signout")
  public ResponseEntity<HttpStatus> signOut(@CookieValue(name = "buy-01") String token) {
    if (token == null) {
      return new ResponseEntity<HttpStatus>(HttpStatus.FORBIDDEN);
    }
    ResponseCookie cookie =
        ResponseCookie.from("buy-01", token)
            .httpOnly(false)
            .sameSite("Lax")
            .domain(cookieDomain)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(null);
  }
}
