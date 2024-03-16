package com.gritlab.buy01.mediaservice.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleMediaResponse {
  String id;
  String image;
  String productId;
  String userId;
  String mimeType;
}
