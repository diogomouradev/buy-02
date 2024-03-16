package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class ProductOwnershipResponseTests {

  @Test
  public void testEquals_SameObject() {
    ProductOwnershipResponse response = new ProductOwnershipResponse();
    assertEquals(response, response);
  }

  @Test
  public void testEquals_EqualObjects() {
    ProductOwnershipResponse response1 = new ProductOwnershipResponse();
    ProductOwnershipResponse response2 = new ProductOwnershipResponse();
    assertEquals(response1, response2);
  }

  @Test
  public void testEquals_NotEqualObjects() {
    ProductOwnershipResponse response1 = new ProductOwnershipResponse();
    ProductOwnershipResponse response2 = new ProductOwnershipResponse();
    response2.setProductId("123");
    assertNotEquals(response1, response2);
  }

  @Test
  public void testEquals_NullObject() {
    ProductOwnershipResponse response = new ProductOwnershipResponse();
    assertNotEquals(response, null);
  }

  @Test
  public void testHashCode_EqualObjects() {
    ProductOwnershipResponse response1 = new ProductOwnershipResponse();
    ProductOwnershipResponse response2 = new ProductOwnershipResponse();
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  public void testHashCode_NotEqualObjects() {
    ProductOwnershipResponse response1 = new ProductOwnershipResponse();
    ProductOwnershipResponse response2 = new ProductOwnershipResponse();
    response1.setProductId("123");
    response2.setProductId("456");
    assertNotEquals(response1.hashCode(), response2.hashCode());
  }
}
