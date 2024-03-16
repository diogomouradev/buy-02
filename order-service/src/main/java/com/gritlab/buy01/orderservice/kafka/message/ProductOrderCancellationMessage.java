package com.gritlab.buy01.orderservice.kafka.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductOrderCancellationMessage {
  String correlationId;
  String productId;
  Integer quantity;
}
