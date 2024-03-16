package com.gritlab.buy01.mediaservice.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOwnershipResponse {
  String productId;
  String userId;
  boolean isOwner;
  String correlationId;
}
