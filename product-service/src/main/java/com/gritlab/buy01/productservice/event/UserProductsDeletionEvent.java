package com.gritlab.buy01.productservice.event;

import org.springframework.context.ApplicationEvent;

public class UserProductsDeletionEvent extends ApplicationEvent {
  private String userId;

  public UserProductsDeletionEvent(Object source, String userId) {
    super(source);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
