package com.gritlab.buy01.productservice.event;

import org.springframework.context.ApplicationEvent;

public class ProductOrderCancellationEvent extends ApplicationEvent {
  private String correlationId;
  private String productId;
  private Integer quantity;

  public ProductOrderCancellationEvent(
      Object source, String correlationId, String productId, Integer quantity) {
    super(source);
    this.correlationId = correlationId;
    this.productId = productId;
    this.quantity = quantity;
  }

  public String getCorrelationId() {
    return this.correlationId;
  }

  public String getProductId() {
    return this.productId;
  }

  public Integer getQuantity() {
    return this.quantity;
  }
}
