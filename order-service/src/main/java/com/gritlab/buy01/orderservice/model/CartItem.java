package com.gritlab.buy01.orderservice.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.dto.ProductDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
@Data
public class CartItem {
  @Id private String id;

  @NotNull private String sellerId;

  @NotNull private String buyerId;

  @NotNull private ProductDTO product;

  @NotNull
  @Min(0)
  private Integer quantity;

  @NotNull private Date addedToCartAt;

  public CartItem(String sellerId, String buyerId, ProductDTO product, Integer quantity) {
    this.sellerId = sellerId;
    this.buyerId = buyerId;
    this.product = product;
    this.quantity = quantity;
    this.addedToCartAt = new Date();
  }

  public CartItem(CartItemDTO cartItemDTO) {
    this.sellerId = cartItemDTO.getSellerId();
    this.buyerId = cartItemDTO.getBuyerId();
    this.product = cartItemDTO.getProduct();
    this.quantity = cartItemDTO.getQuantity();
    this.addedToCartAt = new Date();
  }

  public CartItemDTO convertToDTO() {
    return new CartItemDTO(
        this.getId(),
        this.getSellerId(),
        this.getBuyerId(),
        this.getProduct(),
        this.getQuantity(),
        this.getAddedToCartAt());
  }
}
