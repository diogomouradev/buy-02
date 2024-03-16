package com.gritlab.buy01.orderservice.model;

import lombok.Data;

@Data
public class User {
  private String id;

  private String name;

  private String role;

  public User(String id, String name, String role) {
    this.id = id;
    this.name = name;
    this.role = role;
  }
}
