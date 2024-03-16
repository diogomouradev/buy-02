package com.gritlab.buy01.orderservice.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductDTO implements Serializable {
  private String id;

  @NotNull
  @Size(min = 3, max = 50, message = "Name has to be between 3 and 50 characters long")
  private String name;

  @NotNull
  @Size(min = 3, max = 300, message = "Description has to be between 3 and 300 characters long")
  private String description;

  @NotNull
  @DecimalMin(value = "0.0", message = "Price must be a positive value or zero")
  @DecimalMax(value = "9999999999.0", message = "Maximum price is 9999999999")
  private Double price;

  @NotNull
  @Min(value = 0, message = "Quantity cannot be less than 0")
  private Integer quantity;

  private String userId;

  private transient List<MultipartFile> images;

  public ProductDTO(
      String name, String description, Double price, Integer quantity, String userId) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
    this.userId = userId;
  }

  public ProductDTO(
      String id, String name, String description, Double price, Integer quantity, String userId) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.quantity = quantity;
    this.userId = userId;
  }
}
