package com.gritlab.buy01.mediaservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.bson.types.Binary;
import org.junit.jupiter.api.Test;

public class MediaTests {

  @Test
  public void testEquals() {
    Binary image1 = new Binary(new byte[] {1, 2, 3});
    Media media1 = new Media(image1, "product123", "user456");
    media1.setId("id123");
    media1.setMimeType("image/jpeg");

    Binary image2 = new Binary(new byte[] {1, 2, 3});
    Media media2 = new Media(image2, "product123", "user456");
    media2.setId("id123");
    media2.setMimeType("image/jpeg");

    assertEquals(media1, media2);
  }

  @Test
  public void testNotEquals() {
    Binary image1 = new Binary(new byte[] {1, 2, 3});
    Media media1 = new Media(image1, "product123", "user456");
    media1.setId("id123");
    media1.setMimeType("image/jpeg");

    Binary image2 = new Binary(new byte[] {4, 5, 6});
    Media media2 = new Media(image2, "product789", "user101");
    media2.setId("id789");
    media2.setMimeType("image/png");

    assertNotEquals(media1, media2);
  }

  @Test
  public void testHashCode() {
    Binary image = new Binary(new byte[] {1, 2, 3});
    Media media = new Media(image, "product123", "user456");
    media.setId("id123");
    media.setMimeType("image/jpeg");

    int hashCode = media.hashCode();
    assertNotEquals(0, hashCode);
  }

  @Test
  public void testEquals2() {
    Binary image1 = new Binary(new byte[] {1, 2, 3});
    Media media1 = new Media(image1, "product123", "user456");
    media1.setId("id123");
    media1.setMimeType("image/jpeg");

    Binary image2 = new Binary(new byte[] {1, 2, 3});
    Media media2 = new Media(image2, "product123", "user456");
    media2.setId("id123");
    media2.setMimeType("image/jpeg");

    assertEquals(media1, media2);

    assertEquals(media1, media1);

    assertEquals(media2, media2);

    Media media3 = new Media(image1, "product123", "user456");
    media3.setId("id123");
    media3.setMimeType("image/jpeg");

    assertEquals(media1, media3);

    assertEquals(media2, media3);
  }

  @Test
  public void testNotEquals2() {
    Binary image1 = new Binary(new byte[] {1, 2, 3});
    Media media1 = new Media(image1, "product123", "user456");
    media1.setId("id123");
    media1.setMimeType("image/jpeg");

    Binary image2 = new Binary(new byte[] {4, 5, 6});
    Media media2 = new Media(image2, "product789", "user101");
    media2.setId("id789");
    media2.setMimeType("image/png");

    assertNotEquals(media1, media2);
  }

  @Test
  public void testHashCode2() {
    Binary image = new Binary(new byte[] {1, 2, 3});
    Media media = new Media(image, "product123", "user456");
    media.setId("id123");
    media.setMimeType("image/jpeg");

    int hashCode = media.hashCode();
    assertNotEquals(0, hashCode);
  }
}
