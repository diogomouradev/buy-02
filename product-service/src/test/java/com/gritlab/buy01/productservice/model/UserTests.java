package com.gritlab.buy01.productservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserTests {

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("123", "John Doe", "Admin");
  }

  @Test
  void testId() {
    assertEquals("123", user.getId());
    user.setId("456");
    assertEquals("456", user.getId());
  }

  @Test
  void testName() {
    assertEquals("John Doe", user.getName());
    user.setName("Jane Doe");
    assertEquals("Jane Doe", user.getName());
  }

  @Test
  void testRole() {
    assertEquals("Admin", user.getRole());
    user.setRole("User");
    assertEquals("User", user.getRole());
  }

  @Test
  void testConstructor() {
    User newUser = new User("789", "Test User", "Guest");
    assertEquals("789", newUser.getId());
    assertEquals("Test User", newUser.getName());
    assertEquals("Guest", newUser.getRole());
  }
}
