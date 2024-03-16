package com.gritlab.buy01.userservice.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.model.enums.Role;
import com.gritlab.buy01.userservice.payload.request.LoginRequest;
import com.gritlab.buy01.userservice.payload.request.SignupRequest;
import com.gritlab.buy01.userservice.payload.response.ErrorMessage;
import com.gritlab.buy01.userservice.payload.response.MessageResponse;
import com.gritlab.buy01.userservice.payload.response.UserInfoResponse;
import com.gritlab.buy01.userservice.repository.UserRepository;
import com.gritlab.buy01.userservice.security.UserDetailsImpl;
import com.gritlab.buy01.userservice.security.jwt.JwtUtils;
import com.gritlab.buy01.userservice.utils.EmailValidator;

@Service
public class AuthService {

  @Autowired AuthenticationManager authenticationManager;

  @Autowired UserRepository userRepository;

  @Autowired PasswordEncoder encoder;

  @Autowired JwtUtils jwtUtils;

  @Value("${buy01.app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${app.cookie.domain}")
  private String cookieDomain;

  public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getName(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      String jwtToken = jwtUtils.generateTokenFromUserId(userDetails.getId());

      ResponseCookie jwtCookie =
          ResponseCookie.from("buy-01", jwtToken)
              .httpOnly(false)
              .sameSite("Lax")
              .domain(cookieDomain)
              .secure(true)
              .path("/")
              .maxAge(jwtExpirationMs / 1000)
              .build();

      String role =
          userDetails.getAuthorities().stream()
              .findFirst()
              .map(item -> item.getAuthority())
              .orElse("user");
      ResponseEntity<UserInfoResponse> resp =
          ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
              .body(
                  new UserInfoResponse(
                      userDetails.getId(),
                      userDetails.getUsername(),
                      userDetails.getEmail(),
                      role));
      return resp;
    } catch (Exception e) {
      ErrorMessage error =
          new ErrorMessage("Incorrect username or password", HttpStatus.UNAUTHORIZED.value());
      return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
  }

  public ResponseEntity<?> registerUser(SignupRequest signupRequest) {
    if (!(signupRequest.getRole().equals(Role.CLIENT)
        || signupRequest.getRole().equals(Role.SELLER))) {
      ErrorMessage error =
          new ErrorMessage("Invalid role in signup request", HttpStatus.BAD_REQUEST.value());
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    if (userRepository.existsByName(signupRequest.getName())) {
      ErrorMessage error =
          new ErrorMessage("Username is already taken", HttpStatus.BAD_REQUEST.value());
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    signupRequest.setEmail(signupRequest.getEmail().toLowerCase(Locale.ROOT));
    if (!EmailValidator.isValidEmail(signupRequest.getEmail())) {
      ErrorMessage error = new ErrorMessage("Invalid email format", HttpStatus.BAD_REQUEST.value());
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    if (userRepository.existsByEmail(signupRequest.getEmail())) {
      ErrorMessage error =
          new ErrorMessage("Email is already in use", HttpStatus.BAD_REQUEST.value());
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Create new user's account
    User user =
        new User(
            signupRequest.getName(),
            signupRequest.getEmail(),
            encoder.encode(signupRequest.getPassword()),
            signupRequest.getRole());

    userRepository.save(user);

    return new ResponseEntity<>(
        new MessageResponse("User registered successfully!"), HttpStatus.CREATED);
  }

  public User getUserDetailsFromToken(String token) {
    try {
      String userId = jwtUtils.getUserIdFromJwtToken(token);

      return userRepository.findById(userId).orElse(null);
    } catch (Exception e) {
      return null;
    }
  }

  public User checkAuth(String token) {
    String userId = jwtUtils.getUserIdFromJwtToken(token);
    User user = userRepository.findById(userId).orElse(null);
    return user;
  }
}
