package com.gritlab.buy01.orderservice.kafka.message;

import java.io.Serializable;

import com.gritlab.buy01.orderservice.dto.Cart;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartValidationRequest implements Serializable {
  String correlationId;
  Cart cart;
}
