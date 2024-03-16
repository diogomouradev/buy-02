package com.gritlab.buy01.orderservice.dto;

import java.io.Serializable;
import java.util.Set;

import com.gritlab.buy01.orderservice.model.Order;

import lombok.Data;

@Data
public class OrderModifications implements Serializable {
  private Set<String> notes;

  private Order[] modifications;
}
