package com.gritlab.buy01.userservice.kafka.message;

import lombok.Data;

@Data
public class UserAvatarDeleteMessage {
  String correlationId;
  String userId;

  public UserAvatarDeleteMessage() {}

  public UserAvatarDeleteMessage(String correlationId, String userId) {
    this.correlationId = correlationId;
    this.userId = userId;
  }
}
