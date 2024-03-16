package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UserAvatarDeleteMessageTests {

  @Test
  public void testEquals_SameObject() {
    UserAvatarDeleteMessage message = new UserAvatarDeleteMessage();
    assertEquals(message, message);
  }

  @Test
  public void testEquals_EqualObjects() {
    UserAvatarDeleteMessage message1 = new UserAvatarDeleteMessage();
    UserAvatarDeleteMessage message2 = new UserAvatarDeleteMessage();
    assertEquals(message1, message2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    UserAvatarDeleteMessage message1 = new UserAvatarDeleteMessage();
    UserAvatarDeleteMessage message2 = new UserAvatarDeleteMessage();
    message2.setCorrelationId("123");
    assertNotEquals(message1, message2);
  }

  @Test
  public void testEquals_NullObject() {
    UserAvatarDeleteMessage message = new UserAvatarDeleteMessage();
    assertNotEquals(message, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    UserAvatarDeleteMessage message1 = new UserAvatarDeleteMessage();
    UserAvatarDeleteMessage message2 = new UserAvatarDeleteMessage();
    assertEquals(message1.hashCode(), message2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    UserAvatarDeleteMessage message1 = new UserAvatarDeleteMessage();
    UserAvatarDeleteMessage message2 = new UserAvatarDeleteMessage();
    message2.setCorrelationId("123");
    assertNotEquals(message1.hashCode(), message2.hashCode());
  }
}
