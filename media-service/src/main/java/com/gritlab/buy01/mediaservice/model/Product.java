package com.gritlab.buy01.mediaservice.model;

import lombok.Data;

@Data
public class Product {
  private String id;
  private String name;
  private String description;
  private Double price;
  private Integer quantity;
  private String userId;
}
