package com.gritlab.buy01.orderservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import com.gritlab.buy01.orderservice.dto.ProductDTO;
import com.gritlab.buy01.orderservice.model.enums.OrderStatus;

public class OrderTests {

  private Order order;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    ProductDTO productDTO = new ProductDTO();
    productDTO.setId("product-123");
    productDTO.setName("Sample Product");
    productDTO.setPrice(10.0);

    order = new Order("seller-1", "buyer-1", productDTO, 2);
  }

  @Test
  public void testOrderInitialization() {
    assertNotNull(order);
    assertEquals("seller-1", order.getSellerId());
    assertEquals("buyer-1", order.getBuyerId());
    assertNotNull(order.getProduct());
    assertEquals("product-123", order.getProduct().getId());
    assertEquals(2, order.getQuantity());
    assertNull(order.getStatus());
    assertNull(order.getOrderPlacedAt());
  }

  @Test
  public void testSettersAndGetters() {
    order.setId("order-456");
    assertEquals("order-456", order.getId());

    order.setSellerId("seller-2");
    assertEquals("seller-2", order.getSellerId());

    order.setBuyerId("buyer-2");
    assertEquals("buyer-2", order.getBuyerId());

    ProductDTO newProduct = new ProductDTO();
    newProduct.setId("new-product-789");
    order.setProduct(newProduct);
    assertEquals("new-product-789", order.getProduct().getId());

    order.setQuantity(3);
    assertEquals(3, order.getQuantity());

    order.setStatus(OrderStatus.CONFIRMED);
    assertEquals(OrderStatus.CONFIRMED, order.getStatus());

    Date newDate = new Date();
    order.setOrderPlacedAt(newDate);
    assertEquals(newDate, order.getOrderPlacedAt());
  }

  @Test
  public void testParameterizedConstructor() {
    Order orderWithConstructor = new Order("seller-3", "buyer-3", null, 1);
    assertNotNull(orderWithConstructor);
    assertEquals("seller-3", orderWithConstructor.getSellerId());
    assertEquals("buyer-3", orderWithConstructor.getBuyerId());
    assertNull(orderWithConstructor.getProduct());
    assertEquals(1, orderWithConstructor.getQuantity());
    assertNull(orderWithConstructor.getStatus());
    assertNull(orderWithConstructor.getOrderPlacedAt());
  }
}
