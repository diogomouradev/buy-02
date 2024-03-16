package com.gritlab.buy01.productservice.dto;

import java.io.Serializable;

import com.gritlab.buy01.productservice.model.Order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cart implements Serializable {
  private Order[] orders;
}
