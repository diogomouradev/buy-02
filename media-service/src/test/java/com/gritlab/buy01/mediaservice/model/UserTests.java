package com.gritlab.buy01.mediaservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UserTests {

  @Test
  public void testEquals() {
    User user1 = new User("id123", "Alice", "ADMIN");
    User user2 = new User("id123", "Alice", "ADMIN");

    assertEquals(user1, user2);

    assertEquals(user1, user1);

    assertEquals(user2, user2);
  }

  @Test
  public void testNotEquals() {
    User user1 = new User("id123", "Alice", "ADMIN");
    User user2 = new User("id456", "Bob", "CLIENT");

    assertNotEquals(user1, user2);
  }

  @Test
  public void testHashCode() {
    User user = new User("id123", "Alice", "ADMIN");

    int hashCode = user.hashCode();
    assertNotEquals(0, hashCode);
  }
}
