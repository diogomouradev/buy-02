package com.gritlab.buy01.userservice.payload.response;

import com.gritlab.buy01.userservice.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
  @Size(min = 3, max = 40, message = "Error: Name has to be between 3 and 40 characters long")
  private String name;

  @Email(message = "Invalid email format")
  @Size(min = 0, max = 320, message = "Invalid email length")
  private String email;

  private String password;
  private Role role;
}
