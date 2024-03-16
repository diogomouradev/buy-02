package com.gritlab.buy01.orderservice.kafka.message;

import lombok.Data;

@Data
public class TokenValidationRequest {
  private String jwtToken;
  private String correlationId;

  public TokenValidationRequest(String token, String correlationId) {
    this.jwtToken = token;
    this.correlationId = correlationId;
  }
}
