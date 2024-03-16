package com.gritlab.buy01.userservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.model.enums.Role;
import com.gritlab.buy01.userservice.security.PrincipalData;
import com.gritlab.buy01.userservice.service.UserService;

@WebMvcTest(UserController.class)
public class UserControllerTest {
  private final MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private PrincipalData principalData;

  @Autowired
  public UserControllerTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    reset(userService);
  }

  @Test
  void contextLoads() throws Exception {
    assertThat(mockMvc).isNotNull();
    assertThat(userService).isNotNull();
  }

  @Test
  @WithMockUser
  void testGetAllUsersFound() throws Exception {
    List<User> users =
        Arrays.asList(
            new User("First", "first@email.com", "firstPassword", Role.CLIENT),
            new User("Second", "second@email.com", "secondPassword", Role.CLIENT),
            new User("Third", "third@email.com", "thirdPassword", Role.SELLER),
            new User("admin", "admin@email.com", "adminPassword", Role.ADMIN));

    when(userService.getAllUsers(null)).thenReturn(users);

    mockMvc
        .perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(4)));

    verify(userService).getAllUsers(null);
  }

  @Test
  @WithMockUser
  void testGetAllUsersNotFound() throws Exception {
    when(userService.getAllUsers(anyString())).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/users")).andExpect(status().isNoContent());

    verify(userService).getAllUsers(null);
  }

  @Test
  @WithMockUser
  void testGetUserByIdFound() throws Exception {
    User user = new User("a", "a@b.com", "pass", Role.CLIENT);
    when(userService.getUserById("someId")).thenReturn(Optional.of(user));

    mockMvc.perform(get("/api/users/someId")).andExpect(status().isOk());

    verify(userService).getUserById("someId");
  }

  @Test
  @WithMockUser
  void testGetUserByIdNotFound() throws Exception {
    when(userService.getUserById("someId")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/users/someId")).andExpect(status().isNotFound());

    verify(userService).getUserById("someId");
  }
}
