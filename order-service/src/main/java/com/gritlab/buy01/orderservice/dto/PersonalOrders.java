package com.gritlab.buy01.orderservice.dto;

import com.gritlab.buy01.orderservice.model.Order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonalOrders {
  private Order[] pending;
  private Order[] confirmed;
  private Order[] cancelled;
}
