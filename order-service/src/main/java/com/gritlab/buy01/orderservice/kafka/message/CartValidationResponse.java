package com.gritlab.buy01.orderservice.kafka.message;

import java.io.Serializable;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.OrderModifications;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartValidationResponse implements Serializable {
  private String correlationId;

  private boolean processed;

  private Cart cart;

  // this contains all the modifications for the unprocessed orders
  private OrderModifications orderModifications;
}
