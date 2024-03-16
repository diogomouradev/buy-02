package com.gritlab.buy01.orderservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;

import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.dto.ProductDTO;

class CartItemTest {

  @Test
  void testDefaultConstructor() {
    CartItem cartItem = new CartItem();
    assertNull(cartItem.getId());
    assertNull(cartItem.getSellerId());
    assertNull(cartItem.getBuyerId());
    assertNull(cartItem.getProduct());
    assertNull(cartItem.getQuantity());
    assertNull(cartItem.getAddedToCartAt());
  }

  @Test
  void testAllArgsConstructor() {
    ProductDTO product = new ProductDTO("TestName", "TestDescription", 100.0, 5, "TestUserId");
    CartItem cartItem =
        new CartItem("TestId", "TestSellerId", "TestBuyerId", product, 10, new Date());

    assertEquals("TestId", cartItem.getId());
    assertEquals("TestSellerId", cartItem.getSellerId());
    assertEquals("TestBuyerId", cartItem.getBuyerId());
    assertEquals(product, cartItem.getProduct());
    assertEquals(10, cartItem.getQuantity());
    assertNotNull(cartItem.getAddedToCartAt());
  }

  @Test
  void testCustomConstructor() {
    ProductDTO product = new ProductDTO("TestName", "TestDescription", 100.0, 5, "TestUserId");
    CartItem cartItem = new CartItem("TestSellerId", "TestBuyerId", product, 10);

    assertNull(cartItem.getId());
    assertEquals("TestSellerId", cartItem.getSellerId());
    assertEquals("TestBuyerId", cartItem.getBuyerId());
    assertEquals(product, cartItem.getProduct());
    assertEquals(10, cartItem.getQuantity());
    assertNotNull(cartItem.getAddedToCartAt());
  }

  @Test
  void testConstructorWithCartItemDTO() {
    ProductDTO product = new ProductDTO("TestName", "TestDescription", 100.0, 5, "TestUserId");
    CartItemDTO cartItemDTO = new CartItemDTO("TestId", "TestSellerId", "TestBuyerId", product, 10);
    CartItem cartItem = new CartItem(cartItemDTO);

    assertEquals("TestSellerId", cartItem.getSellerId());
    assertEquals("TestBuyerId", cartItem.getBuyerId());
    assertEquals(product, cartItem.getProduct());
    assertEquals(10, cartItem.getQuantity());
    assertNotNull(cartItem.getAddedToCartAt());
  }

  @Test
  void testSettersAndGetters() {
    CartItem cartItem = new CartItem();
    cartItem.setId("TestId");
    cartItem.setSellerId("TestSellerId");
    cartItem.setBuyerId("TestBuyerId");
    cartItem.setQuantity(10);
    cartItem.setAddedToCartAt(new Date());

    assertEquals("TestId", cartItem.getId());
    assertEquals("TestSellerId", cartItem.getSellerId());
    assertEquals("TestBuyerId", cartItem.getBuyerId());
    assertEquals(10, cartItem.getQuantity());
    assertNotNull(cartItem.getAddedToCartAt());
  }
}
