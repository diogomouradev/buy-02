package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ProductMediaDeleteMessageTests {

  @Test
  public void testEquals_SameObject() {
    ProductMediaDeleteMessage message = new ProductMediaDeleteMessage("correlationId", "productId");
    assertTrue(message.equals(message));
  }

  @Test
  public void testEquals_EqualObjects() {
    ProductMediaDeleteMessage message1 =
        new ProductMediaDeleteMessage("correlationId", "productId");
    ProductMediaDeleteMessage message2 =
        new ProductMediaDeleteMessage("correlationId", "productId");
    assertTrue(message1.equals(message2));
  }

  @Test
  public void testEquals_NotEqualObjects() {
    ProductMediaDeleteMessage message1 =
        new ProductMediaDeleteMessage("correlationId1", "productId1");
    ProductMediaDeleteMessage message2 =
        new ProductMediaDeleteMessage("correlationId2", "productId2");
    assertFalse(message1.equals(message2));
  }

  @Test
  public void testEquals_NullObject() {
    ProductMediaDeleteMessage message = new ProductMediaDeleteMessage("correlationId", "productId");
    assertFalse(message.equals(null));
  }

  @Test
  public void testHashCode_EqualObjects() {
    ProductMediaDeleteMessage message1 =
        new ProductMediaDeleteMessage("correlationId", "productId");
    ProductMediaDeleteMessage message2 =
        new ProductMediaDeleteMessage("correlationId", "productId");
    assertEquals(message1.hashCode(), message2.hashCode());
  }
}
