package com.gritlab.buy01.productservice.payload.response;

import java.util.List;

import com.gritlab.buy01.productservice.model.ProductModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductCreationResponse {
  private ProductModel product;
  private List<String> errors;

  public void addError(String error) {
    this.errors.add(error);
  }
}
