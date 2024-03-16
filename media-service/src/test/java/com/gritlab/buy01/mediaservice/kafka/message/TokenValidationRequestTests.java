package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class TokenValidationRequestTests {

  @Test
  public void testEquals_SameObject() {
    TokenValidationRequest request = new TokenValidationRequest("token", "correlationId");
    assertEquals(request, request);
  }

  @Test
  public void testEquals_EqualObjects() {
    TokenValidationRequest request1 = new TokenValidationRequest("token", "correlationId");
    TokenValidationRequest request2 = new TokenValidationRequest("token", "correlationId");
    assertEquals(request1, request2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    TokenValidationRequest request1 = new TokenValidationRequest("token1", "correlationId");
    TokenValidationRequest request2 = new TokenValidationRequest("token2", "correlationId");
    assertNotEquals(request1, request2);
  }

  @Test
  public void testEquals_NullObject() {
    TokenValidationRequest request = new TokenValidationRequest("token", "correlationId");
    assertNotEquals(request, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    TokenValidationRequest request1 = new TokenValidationRequest("token", "correlationId");
    TokenValidationRequest request2 = new TokenValidationRequest("token", "correlationId");
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    TokenValidationRequest request1 = new TokenValidationRequest("token1", "correlationId");
    TokenValidationRequest request2 = new TokenValidationRequest("token2", "correlationId");
    assertNotEquals(request1.hashCode(), request2.hashCode());
  }
}
