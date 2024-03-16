package com.gritlab.buy01.mediaservice.kafka.message;

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
