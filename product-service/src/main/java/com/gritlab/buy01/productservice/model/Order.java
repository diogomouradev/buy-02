package com.gritlab.buy01.productservice.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.gritlab.buy01.productservice.model.enums.OrderStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Document(collection = "orders")
@Data
public class Order implements Serializable {
  @Id private String id;

  private String sellerId;

  private String buyerId;

  private ProductDTO product;

  private Integer quantity;

  private OrderStatus status;

  private Date orderPlacedAt;

  public Order(String sellerId, String buyerId, ProductDTO product, Integer quantity) {
    this.sellerId = sellerId;
    this.buyerId = buyerId;
    this.product = product;
    this.quantity = quantity;

    this.status = OrderStatus.PENDING;
  }
}
