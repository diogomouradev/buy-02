package com.gritlab.buy01.productservice.event;

import org.springframework.context.ApplicationEvent;

import com.gritlab.buy01.productservice.dto.Cart;

public class CartValidationEvent extends ApplicationEvent {
  private final String correlationId;
  private final Cart cart;

  public CartValidationEvent(Object source, String correlationId, Cart cart) {
    super(source);
    this.correlationId = correlationId;
    this.cart = cart;
  }

  public String getCorrelationId() {
    return this.correlationId;
  }

  public Cart getCart() {
    return this.cart;
  }
}
