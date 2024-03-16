package com.gritlab.buy01.productservice.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.gritlab.buy01.productservice.model.User;
import com.gritlab.buy01.productservice.security.UserDetailsImpl;

public class UserDetailsImplTests {

  private User user;
  private UserDetailsImpl userDetails;

  @BeforeEach
  public void setup() {
    user = new User("testId", "testName", "SELLER");
    userDetails = UserDetailsImpl.build(user);
  }

  @Test
  public void testBuild() {
    assertEquals("testId", userDetails.getId());
    assertEquals("testName", userDetails.getUsername());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELLER")));
  }

  @Test
  public void testEquals() {
    UserDetailsImpl anotherUserDetails = UserDetailsImpl.build(user);
    assertTrue(userDetails.equals(anotherUserDetails));

    User anotherUser = new User("anotherTestId", "anotherTestName", "SELLER");
    UserDetailsImpl differentUserDetails = UserDetailsImpl.build(anotherUser);
    assertFalse(userDetails.equals(differentUserDetails));
  }
}
