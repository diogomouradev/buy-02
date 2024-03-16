package com.gritlab.buy01.mediaservice.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.mediaservice.model.Media;
import com.gritlab.buy01.mediaservice.payload.response.SingleMediaResponse;

@Service
public class ThumbnailService {

  @Cacheable(cacheNames = "thumbnails", key = "#mediaId")
  public ResponseEntity<?> getProductThumbnailHelper(String mediaId, Media media) {
    return getCachedThumbnail(mediaId, media);
  }

  private ResponseEntity<?> getCachedThumbnail(String mediaId, Media media) {
    try {
      BufferedImage originalImage =
          ImageIO.read(new ByteArrayInputStream(media.getImage().getData()));
      BufferedImage thumbnailImage = createThumbnail(originalImage, 400, 400);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(thumbnailImage, "jpg", baos);
      byte[] thumbnailBytes = baos.toByteArray();
      String base64Thumbnail = Base64.getEncoder().encodeToString(thumbnailBytes);

      SingleMediaResponse response =
          new SingleMediaResponse(
              media.getId(),
              base64Thumbnail,
              media.getProductId(),
              media.getUserId(),
              "image/jpeg");
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public BufferedImage createThumbnail(BufferedImage img, int maxWidth, int maxHeight) {
    int originalWidth = img.getWidth();
    int originalHeight = img.getHeight();
    int newWidth = originalWidth;
    int newHeight = originalHeight;

    if (originalWidth > maxWidth) {
      newWidth = maxWidth;
      newHeight = (newWidth * originalHeight) / originalWidth;
    }

    if (newHeight > maxHeight) {
      newHeight = maxHeight;
      newWidth = (newHeight * originalWidth) / originalHeight;
    }

    Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = thumbnail.createGraphics();
    g2d.drawImage(scaledImage, 0, 0, null);
    g2d.dispose();

    return thumbnail;
  }
}
