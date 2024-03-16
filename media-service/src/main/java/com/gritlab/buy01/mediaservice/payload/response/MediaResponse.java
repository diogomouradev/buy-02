package com.gritlab.buy01.mediaservice.payload.response;

import java.util.List;

import com.gritlab.buy01.mediaservice.model.Media;

import lombok.Data;

@Data
public class MediaResponse {
  String productId;
  List<Media> media;
}
