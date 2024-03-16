package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class TokenValidationResponseTests {

  @Test
  public void testEquals_SameObject() {
    TokenValidationResponse response = new TokenValidationResponse();
    assertEquals(response, response);
  }

  @Test
  public void testEquals_EqualObjects() {
    TokenValidationResponse response1 = new TokenValidationResponse();
    TokenValidationResponse response2 = new TokenValidationResponse();
    assertEquals(response1, response2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    TokenValidationResponse response1 = new TokenValidationResponse();
    TokenValidationResponse response2 = new TokenValidationResponse();
    response2.setUserId("123");
    assertNotEquals(response1, response2);
  }

  @Test
  public void testEquals_NullObject() {
    TokenValidationResponse response = new TokenValidationResponse();
    assertNotEquals(response, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    TokenValidationResponse response1 = new TokenValidationResponse();
    TokenValidationResponse response2 = new TokenValidationResponse();
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    TokenValidationResponse response1 = new TokenValidationResponse();
    TokenValidationResponse response2 = new TokenValidationResponse();
    response2.setUserId("123");
    assertNotEquals(response1.hashCode(), response2.hashCode());
  }
}
