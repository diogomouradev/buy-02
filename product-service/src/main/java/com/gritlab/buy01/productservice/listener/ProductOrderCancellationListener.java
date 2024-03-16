package com.gritlab.buy01.productservice.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.gritlab.buy01.productservice.event.ProductOrderCancellationEvent;
import com.gritlab.buy01.productservice.service.ProductService;

@Component
public class ProductOrderCancellationListener {
  private final ProductService productService;

  @Autowired
  public ProductOrderCancellationListener(ProductService productService) {
    this.productService = productService;
  }

  @EventListener
  public void handleProductOrderCancellation(ProductOrderCancellationEvent event) {
    productService.handleProductOrderCancellation(event.getProductId(), event.getQuantity());
  }
}
