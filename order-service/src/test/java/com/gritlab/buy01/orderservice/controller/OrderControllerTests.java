package com.gritlab.buy01.orderservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gritlab.buy01.orderservice.dto.OrderStatusUpdate;
import com.gritlab.buy01.orderservice.dto.PersonalOrders;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.model.Order;
import com.gritlab.buy01.orderservice.payload.response.ErrorMessage;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;
import com.gritlab.buy01.orderservice.service.KafkaService;
import com.gritlab.buy01.orderservice.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
  private static final String CLIENT = "ROLE_CLIENT";
  private static final String SELLER = "ROLE_SELLER";

  @Mock private OrderService orderService;
  @Mock private KafkaService kafkaService;

  @InjectMocks private OrderController orderController;

  @Mock private UserDetailsImpl mockUserDetails;

  private void setUpWithRole(String role) {
    mockUserDetails =
        new UserDetailsImpl(
            "userId", "userName", Collections.singletonList(new SimpleGrantedAuthority(role)));

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient()
        .when(securityContext.getAuthentication())
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void getOrdersSuccess() throws Exception {
    setUpWithRole(CLIENT);
    when(orderService.getOrders(any(UserDetailsImpl.class)))
        .thenReturn(new PersonalOrders(new Order[] {}, new Order[] {}, new Order[] {}));

    ResponseEntity<Object> response = orderController.getOrders();
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void getOrderNotFound() {
    setUpWithRole(CLIENT);
    lenient().when(orderService.getOrders(any(UserDetailsImpl.class))).thenReturn(null);

    ResponseEntity<Object> response = orderController.getOrders();
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void placeOrderForbidden() throws NotFoundException, TimeoutException {
    setUpWithRole(SELLER);

    ResponseEntity<Object> mockOrder = orderController.placeOrder(null);

    assertEquals(HttpStatus.FORBIDDEN, mockOrder.getStatusCode());
  }

  @Test
  void changeOrderStatusSuccess() throws Exception {
    setUpWithRole(CLIENT);
    OrderStatusUpdate update = new OrderStatusUpdate(); // Create a mock OrderStatusUpdate
    Order mockOrder = new Order(); // Create a mock Order

    when(orderService.changeOrderStatus(any(OrderStatusUpdate.class), anyString(), anyString()))
        .thenReturn(mockOrder);

    ResponseEntity<Object> response = orderController.changeOrderStatus(update);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockOrder, response.getBody());
  }

  @Test
  void changeOrderStatusForbidden() {
    setUpWithRole(SELLER);
    OrderStatusUpdate update = new OrderStatusUpdate();

    when(orderService.changeOrderStatus(any(OrderStatusUpdate.class), anyString(), anyString()))
        .thenThrow(new ForbiddenException("Forbidden"));

    ResponseEntity<Object> response = orderController.changeOrderStatus(update);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertTrue(response.getBody() instanceof ErrorMessage);
  }

  @Test
  void changeOrderStatusNotFound() {
    setUpWithRole(CLIENT);
    OrderStatusUpdate update = new OrderStatusUpdate();

    when(orderService.changeOrderStatus(any(OrderStatusUpdate.class), anyString(), anyString()))
        .thenThrow(new NotFoundException("Not Found"));

    ResponseEntity<Object> response = orderController.changeOrderStatus(update);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(response.getBody() instanceof ErrorMessage);
  }

  @Test
  void changeOrderStatusInternalServerError() {
    setUpWithRole(CLIENT);
    OrderStatusUpdate update = new OrderStatusUpdate();

    when(orderService.changeOrderStatus(any(OrderStatusUpdate.class), anyString(), anyString()))
        .thenThrow(new RuntimeException("Internal Server Error"));

    ResponseEntity<Object> response = orderController.changeOrderStatus(update);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertTrue(response.getBody() instanceof ErrorMessage);
  }
}
