package com.gritlab.buy01.mediaservice.service;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
public class ImageService {

  public BufferedImage readImage(InputStream inputStream) throws IOException {
    return ImageIO.read(inputStream);
  }

  public void writeImage(RenderedImage im, String formatName, OutputStream output)
      throws IOException {
    ImageIO.write(im, formatName, output);
  }
}
