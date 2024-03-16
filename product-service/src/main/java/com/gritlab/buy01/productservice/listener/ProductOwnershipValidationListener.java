package com.gritlab.buy01.productservice.listener;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.productservice.event.ProductOwnershipValidationEvent;
import com.gritlab.buy01.productservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.productservice.model.ProductModel;
import com.gritlab.buy01.productservice.service.ProductService;

@Service
public class ProductOwnershipValidationListener {

  private final ProductService productService;

  @Qualifier("productOwnershipResponseKafkaTemplate")
  private final KafkaTemplate<String, ProductOwnershipResponse> kafkaTemplate;

  @Autowired
  public ProductOwnershipValidationListener(
      ProductService productService,
      KafkaTemplate<String, ProductOwnershipResponse> kafkaTemplate) {
    this.productService = productService;
    this.kafkaTemplate = kafkaTemplate;
  }

  @EventListener
  public void handleProductOwnershipValidationEvent(ProductOwnershipValidationEvent event) {
    String productId = event.getProductId();
    String userId = event.getUserId();
    String correlationId = event.getCorrelationId();
    boolean isOwner = false;

    Optional<ProductModel> product = productService.getProductById(productId);
    if (product.isPresent()) {
      isOwner = product.get().getUserId().equals(userId);
    }

    ProductOwnershipResponse response =
        new ProductOwnershipResponse(productId, userId, isOwner, correlationId);
    kafkaTemplate.send("product-ownership-responses", response);
  }
}
