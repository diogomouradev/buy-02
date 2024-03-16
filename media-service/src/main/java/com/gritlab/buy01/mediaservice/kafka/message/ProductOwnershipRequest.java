package com.gritlab.buy01.mediaservice.kafka.message;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductOwnershipRequest {
  String productId;
  String userId;
  String correlationId;

  public ProductOwnershipRequest(String productId, String userId) {
    this.productId = productId;
    this.userId = userId;
    this.correlationId = UUID.randomUUID().toString();
  }
}
