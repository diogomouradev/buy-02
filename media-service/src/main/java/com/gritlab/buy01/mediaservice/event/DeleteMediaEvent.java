package com.gritlab.buy01.mediaservice.event;

import org.springframework.context.ApplicationEvent;

public class DeleteMediaEvent extends ApplicationEvent {
  private final String userId;
  private final String productId;

  public DeleteMediaEvent(Object source, String userId, String productId) {
    super(source);
    this.userId = userId;
    this.productId = productId;
  }

  public String getUserId() {
    return userId;
  }

  public String getProductId() {
    return productId;
  }
}
