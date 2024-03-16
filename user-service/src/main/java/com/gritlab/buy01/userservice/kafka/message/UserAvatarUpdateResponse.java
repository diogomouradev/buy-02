package com.gritlab.buy01.userservice.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAvatarUpdateResponse {
  private String correlationId;
  private String userId;
  private String avatarId;
  private boolean isAllowed;
  private String message;
}
