package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UserAvatarUpdateResponseTests {

  @Test
  public void testEquals_SameObject() {
    UserAvatarUpdateResponse response = new UserAvatarUpdateResponse();
    assertEquals(response, response);
  }

  @Test
  public void testEquals_EqualObjects() {
    UserAvatarUpdateResponse response1 = new UserAvatarUpdateResponse();
    UserAvatarUpdateResponse response2 = new UserAvatarUpdateResponse();
    assertEquals(response1, response2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    UserAvatarUpdateResponse response1 = new UserAvatarUpdateResponse();
    UserAvatarUpdateResponse response2 = new UserAvatarUpdateResponse();
    response2.setCorrelationId("123");
    assertNotEquals(response1, response2);
  }

  @Test
  public void testEquals_NullObject() {
    UserAvatarUpdateResponse response = new UserAvatarUpdateResponse();
    assertNotEquals(response, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    UserAvatarUpdateResponse response1 = new UserAvatarUpdateResponse();
    UserAvatarUpdateResponse response2 = new UserAvatarUpdateResponse();
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    UserAvatarUpdateResponse response1 = new UserAvatarUpdateResponse();
    UserAvatarUpdateResponse response2 = new UserAvatarUpdateResponse();
    response2.setCorrelationId("123");
    assertNotEquals(response1.hashCode(), response2.hashCode());
  }
}
