package com.gritlab.buy01.mediaservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class ProductTests {

  @Test
  public void testEquals() {
    Product product1 = new Product();
    product1.setId("id123");
    product1.setName("Product A");
    product1.setDescription("Description A");
    product1.setPrice(10.0);
    product1.setQuantity(5);
    product1.setUserId("user456");

    Product product2 = new Product();
    product2.setId("id123");
    product2.setName("Product A");
    product2.setDescription("Description A");
    product2.setPrice(10.0);
    product2.setQuantity(5);
    product2.setUserId("user456");

    assertEquals(product1, product2);

    assertEquals(product1, product1);

    assertEquals(product2, product2);
  }

  @Test
  public void testNotEquals() {
    Product product1 = new Product();
    product1.setId("id123");
    product1.setName("Product A");
    product1.setDescription("Description A");
    product1.setPrice(10.0);
    product1.setQuantity(5);
    product1.setUserId("user456");

    Product product2 = new Product();
    product2.setId("id789");
    product2.setName("Product B");
    product2.setDescription("Description B");
    product2.setPrice(20.0);
    product2.setQuantity(3);
    product2.setUserId("user101");

    assertNotEquals(product1, product2);
  }

  @Test
  public void testHashCode() {
    Product product = new Product();
    product.setId("id123");
    product.setName("Product A");
    product.setDescription("Description A");
    product.setPrice(10.0);
    product.setQuantity(5);
    product.setUserId("user456");

    int hashCode = product.hashCode();
    assertNotEquals(0, hashCode);
  }
}
