package com.gritlab.buy01.orderservice.payload.response;

import lombok.Data;

@Data
public class ErrorMessage {
  int status;
  String message;

  public ErrorMessage(String message, int status) {
    this.message = message;
    this.status = status;
  }
}
