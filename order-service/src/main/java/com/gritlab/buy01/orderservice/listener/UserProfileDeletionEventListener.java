package com.gritlab.buy01.orderservice.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gritlab.buy01.orderservice.event.UserProfileDeletionEvent;
import com.gritlab.buy01.orderservice.service.CartService;

@Component
public class UserProfileDeletionEventListener {
  private final CartService cartService;

  @Autowired
  public UserProfileDeletionEventListener(CartService cartService) {
    this.cartService = cartService;
  }

  @EventListener
  public void handleUserProfileDeletionEvent(UserProfileDeletionEvent event) {
    cartService.deleteUserCart(event.getUserId());
  }
}
