package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UserAvatarUpdateRequestTests {

  @Test
  public void testEquals_SameObject() {
    UserAvatarUpdateRequest request = new UserAvatarUpdateRequest();
    assertEquals(request, request);
  }

  @Test
  public void testEquals_EqualObjects() {
    UserAvatarUpdateRequest request1 = new UserAvatarUpdateRequest();
    UserAvatarUpdateRequest request2 = new UserAvatarUpdateRequest();
    assertEquals(request1, request2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    UserAvatarUpdateRequest request1 = new UserAvatarUpdateRequest();
    UserAvatarUpdateRequest request2 = new UserAvatarUpdateRequest();
    request2.setCorrelationId("123");
    assertNotEquals(request1, request2);
  }

  @Test
  public void testEquals_NullObject() {
    UserAvatarUpdateRequest request = new UserAvatarUpdateRequest();
    assertNotEquals(request, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    UserAvatarUpdateRequest request1 = new UserAvatarUpdateRequest();
    UserAvatarUpdateRequest request2 = new UserAvatarUpdateRequest();
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    UserAvatarUpdateRequest request1 = new UserAvatarUpdateRequest();
    UserAvatarUpdateRequest request2 = new UserAvatarUpdateRequest();
    request2.setCorrelationId("123");
    assertNotEquals(request1.hashCode(), request2.hashCode());
  }
}
