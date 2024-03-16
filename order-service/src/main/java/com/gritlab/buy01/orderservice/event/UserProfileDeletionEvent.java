package com.gritlab.buy01.orderservice.event;

import org.springframework.context.ApplicationEvent;

public class UserProfileDeletionEvent extends ApplicationEvent {
  private String correlationId;
  private String userId;

  public UserProfileDeletionEvent(Object source, String correlationId, String userId) {
    super(source);
    this.correlationId = correlationId;
    this.userId = userId;
  }

  public String getCorrelationId() {
    return this.correlationId;
  }

  public String getUserId() {
    return this.userId;
  }
}
