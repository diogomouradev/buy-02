package com.gritlab.buy01.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.gritlab.buy01.userservice.model.User;
import com.gritlab.buy01.userservice.model.enums.Role;
import com.gritlab.buy01.userservice.payload.response.UserUpdateRequest;
import com.gritlab.buy01.userservice.repository.UserRepository;
import com.gritlab.buy01.userservice.security.UserDetailsServiceImpl;

public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private BCryptPasswordEncoder passwordEncoder;

  @Mock private UserDetailsServiceImpl userDetailsServiceImpl;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private UserService userService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  public void testGetAllUsersWithName() {
    User user = new User("testUser", "test@email.com", "password", Role.CLIENT);
    when(userRepository.findByName("testUser")).thenReturn(List.of(user));

    List<User> users = userService.getAllUsers("testUser");

    assertEquals(1, users.size());
    assertEquals("testUser", users.get(0).getName());
    verify(userRepository).findByName("testUser");
  }

  @Test
  public void testUpdateUser() {
    // Prepare mock data
    User originalUser = new User("originalUser", "original@email.com", "password", Role.CLIENT);
    originalUser.setId("someUniqueID");
    User updatedUser = new User("updatedUser", "updated@email.com", "newPassword", Role.CLIENT);
    UserUpdateRequest userUpdateRequest =
        new UserUpdateRequest(
            updatedUser.getName(),
            updatedUser.getEmail(),
            updatedUser.getPassword(),
            updatedUser.getRole());

    when(userRepository.findById(originalUser.getId())).thenReturn(Optional.of(originalUser));
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    ResponseEntity<?> result =
        userService.updateUser(originalUser.getId(), userUpdateRequest, false);

    assertTrue(result.getBody() instanceof User);
    User returnedUser = (User) result.getBody();
    assertEquals("updatedUser", returnedUser.getName());
    assertEquals("updated@email.com", returnedUser.getEmail());
    assertEquals(Role.CLIENT, returnedUser.getRole());

    verify(userRepository).findById(originalUser.getId());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void testDeleteUser() {
    String id = "someUniqueID";
    when(userRepository.findById(id))
        .thenReturn(Optional.of(new User("name", "email@email.com", "password", Role.CLIENT)));

    doNothing().when(eventPublisher).publishEvent(any());

    userService.deleteUser(id);

    verify(userRepository).deleteById(id);
  }
}
