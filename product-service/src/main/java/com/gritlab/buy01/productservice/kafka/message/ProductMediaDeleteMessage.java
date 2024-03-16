package com.gritlab.buy01.productservice.kafka.message;

import lombok.Data;

@Data
public class ProductMediaDeleteMessage {
  String correlationId;
  String productId;

  public ProductMediaDeleteMessage() {}

  public ProductMediaDeleteMessage(String correlationId, String productId) {
    this.correlationId = correlationId;
    this.productId = productId;
  }
}
