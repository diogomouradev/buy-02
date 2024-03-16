package com.gritlab.buy01.productservice.kafka.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductOrderCancellationMessage {
  String correlationId;
  String productId;
  Integer quantity;
}
