package com.gritlab.buy01.mediaservice.kafka.message;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ProductOwnershipRequestTests {

  @Test
  public void testEquals_SameObject() {
    ProductOwnershipRequest request = new ProductOwnershipRequest("productId", "userId");
    assertTrue(request.equals(request));
  }

  @Test
  public void testEquals_EqualObjects() {
    ProductOwnershipRequest request1 = new ProductOwnershipRequest("productId", "userId");
    ProductOwnershipRequest request2 = new ProductOwnershipRequest("productId", "userId");
    assertFalse(request1.equals(request2));
  }

  @Test
  public void testEquals_NotEqualObjects() {
    ProductOwnershipRequest request1 = new ProductOwnershipRequest("productId1", "userId1");
    ProductOwnershipRequest request2 = new ProductOwnershipRequest("productId2", "userId2");
    assertFalse(request1.equals(request2));
  }

  @Test
  public void testEquals_NullObject() {
    ProductOwnershipRequest request = new ProductOwnershipRequest("productId", "userId");
    assertFalse(request.equals(null));
  }

  @Test
  public void testHashCode_EqualObjects() {
    ProductOwnershipRequest request1 = new ProductOwnershipRequest("productId", "userId");
    ProductOwnershipRequest request2 = new ProductOwnershipRequest("productId", "userId");
    assertFalse(request1.hashCode() == request2.hashCode());
  }
}
