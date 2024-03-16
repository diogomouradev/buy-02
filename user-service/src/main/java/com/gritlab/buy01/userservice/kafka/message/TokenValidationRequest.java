package com.gritlab.buy01.userservice.kafka.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenValidationRequest {
  private String jwtToken;
  private String correlationId;
}
