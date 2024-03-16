package com.gritlab.buy01.productservice.event;

import org.springframework.context.ApplicationEvent;

public class ProductOwnershipValidationEvent extends ApplicationEvent {
  private final String productId;
  private final String userId;
  private final String correlationId;

  public ProductOwnershipValidationEvent(
      Object source, String productId, String userId, String correlationId) {
    super(source);
    this.productId = productId;
    this.userId = userId;
    this.correlationId = correlationId;
  }

  public String getProductId() {
    return this.productId;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getCorrelationId() {
    return this.correlationId;
  }
}
