package com.gritlab.buy01.userservice.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtilsTest {

  private JwtUtils jwtUtils;

  @BeforeEach
  public void setUp() {
    jwtUtils = new JwtUtils();

    // set values for the @Value annotations
    String secretKey =
        Base64.getUrlEncoder()
            .encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());
    ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secretKey);
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000); // 1 day
  }

  @Test
  public void testGenerateTokenFromUserId() {
    String userId = "testUser";
    String token = jwtUtils.generateTokenFromUserId(userId);

    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  public void testGetUserIdFromJwtToken() {
    String userId = "testUser";
    String token = jwtUtils.generateTokenFromUserId(userId);

    String extractedUserId = jwtUtils.getUserIdFromJwtToken(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  public void testValidateJwtToken_validToken() {
    String userId = "testUser";
    String token = jwtUtils.generateTokenFromUserId(userId);

    boolean isValid = jwtUtils.validateJwtToken(token);

    assertTrue(isValid);
  }

  @Test
  public void testValidateJwtToken_expiredToken() {
    String userId = "testUser";
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1);
    String token = jwtUtils.generateTokenFromUserId(userId);

    // let the token expire
    try {
      Thread.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    boolean isValid = jwtUtils.validateJwtToken(token);

    assertFalse(isValid);
  }

  @Test
  public void testValidateJwtToken_malformedToken() {
    boolean isValid = jwtUtils.validateJwtToken("malformedToken");

    assertFalse(isValid);
  }
}
