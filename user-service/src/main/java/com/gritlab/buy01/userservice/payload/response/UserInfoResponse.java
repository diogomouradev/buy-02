package com.gritlab.buy01.userservice.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {
  private String id;
  private String name;
  private String email;
  private String role;

  public UserInfoResponse(String id, String name, String email, String role) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.role = role;
  }
}
