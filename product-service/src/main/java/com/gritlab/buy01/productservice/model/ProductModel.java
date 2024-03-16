package com.gritlab.buy01.productservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Document(collection = "products")
@Data
public class ProductModel {
  @Id private String id;

  @Field
  @NotNull
  @Size(min = 3, max = 50, message = "Name has to be between 3 and 50 characters long")
  private String name;

  @Field
  @NotNull
  @Size(min = 3, max = 300, message = "Description has to be between 3 and 300 characters long")
  private String description;

  @Field
  @NotNull
  @Min(value = 0, message = "Price must be a positive value or zero")
  private Double price;

  @Field
  @NotNull
  @Min(value = 0, message = "Quantity cannot be less than 0")
  private Integer quantity;

  @Field private String userId;

  public ProductModel(
      String name, String description, Double price, Integer quantity, String userId) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
    this.userId = userId;
  }
}
