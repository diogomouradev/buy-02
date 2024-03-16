package com.gritlab.buy01.mediaservice.model;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Document(collection = "media")
public class Media {
  @Id String id;
  @Field @NotNull Binary image;
  @Field String productId;
  @Field String userId;
  @Field String mimeType;

  public Media(Binary image, String productId, String userId) {
    this.image = image;
    this.productId = productId;
    this.userId = userId;
  }
}
