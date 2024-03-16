package com.gritlab.buy01.userservice.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RoleTests {

  @Test
  public void testRoleEnumValues() {
    assertEquals(3, Role.values().length);
    assertEquals(Role.CLIENT, Role.valueOf("CLIENT"));
    assertEquals(Role.SELLER, Role.valueOf("SELLER"));
  }
}
