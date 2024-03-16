package com.gritlab.buy01.orderservice.kafka.message;

import lombok.Data;

@Data
public class TokenValidationResponse {
  private String jwtToken;
  private String userId;
  private String name;
  private String email;
  private String role;
  private String errorMessage;
  private String correlationId;
}
