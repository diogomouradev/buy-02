package com.gritlab.buy01.orderservice.dto;

import java.io.Serializable;

import com.gritlab.buy01.orderservice.model.Order;

import lombok.Data;

@Data
public class Cart implements Serializable {
  private Order[] orders;
}
