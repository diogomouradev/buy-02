package com.gritlab.buy01.userservice.event;

import org.springframework.context.ApplicationEvent;

import com.gritlab.buy01.userservice.model.User;

public class UserDeletionEvent extends ApplicationEvent {
  private final User user;

  public UserDeletionEvent(Object source, User user) {
    super(source);
    this.user = user;
  }

  public User getUser() {
    return user;
  }
}
