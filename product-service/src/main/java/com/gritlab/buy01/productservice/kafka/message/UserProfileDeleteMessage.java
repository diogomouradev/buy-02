package com.gritlab.buy01.productservice.kafka.message;

import lombok.Data;

@Data
public class UserProfileDeleteMessage {
  String correlationId;
  String userId;

  public UserProfileDeleteMessage() {}

  public UserProfileDeleteMessage(String correlationId, String userId) {
    this.correlationId = correlationId;
    this.userId = userId;
  }
}
