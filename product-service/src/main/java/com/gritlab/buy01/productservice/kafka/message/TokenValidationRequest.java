package com.gritlab.buy01.productservice.kafka.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TokenValidationRequest {
  private String jwtToken;
  private String correlationId;

  public TokenValidationRequest(String token, String correlationId) {
    this.jwtToken = token;
    this.correlationId = correlationId;
  }
}
