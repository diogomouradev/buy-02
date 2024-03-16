package com.gritlab.buy01.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
  private Cart cart;

  private Boolean processed;

  private OrderModifications orderModifications;
}
