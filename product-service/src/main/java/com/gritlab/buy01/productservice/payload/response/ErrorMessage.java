package com.gritlab.buy01.productservice.payload.response;

import lombok.Data;

@Data
public class ErrorMessage {
  int status;
  String message;

  public ErrorMessage(int status, String message) {
    this.status = status;
    this.message = message;
  }
}
