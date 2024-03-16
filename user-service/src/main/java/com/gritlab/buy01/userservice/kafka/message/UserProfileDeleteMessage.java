package com.gritlab.buy01.userservice.kafka.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileDeleteMessage {
  private String correlationId;
  private String userId;

  public UserProfileDeleteMessage(String correlationId, String userId) {
    this.correlationId = correlationId;
    this.userId = userId;
  }
}
