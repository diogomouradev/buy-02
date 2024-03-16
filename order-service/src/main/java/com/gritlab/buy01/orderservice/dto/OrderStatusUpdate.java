package com.gritlab.buy01.orderservice.dto;

import com.gritlab.buy01.orderservice.model.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdate {
  private String id;
  private OrderStatus status;
}
