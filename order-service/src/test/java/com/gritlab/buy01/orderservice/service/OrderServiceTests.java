package com.gritlab.buy01.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartResponse;
import com.gritlab.buy01.orderservice.dto.OrderModifications;
import com.gritlab.buy01.orderservice.dto.OrderStatusUpdate;
import com.gritlab.buy01.orderservice.dto.PersonalOrders;
import com.gritlab.buy01.orderservice.dto.ProductDTO;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.orderservice.kafka.message.ProductOrderCancellationMessage;
import com.gritlab.buy01.orderservice.model.Order;
import com.gritlab.buy01.orderservice.model.enums.OrderStatus;
import com.gritlab.buy01.orderservice.repository.OrderRepository;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;

class OrderServiceTests {
  @Mock private OrderRepository orderRepository;
  @Mock private KafkaService kafkaService;
  @Mock private CartService cartService;
  @Mock private SecurityContext securityContext;
  @InjectMocks private OrderService orderService;

  private static final String USER_ID = "userId";
  private static final String ORDER_ID = "orderId";
  private static final String OTHER_USER_ID = "otherUserId";
  private static final String PRODUCT_ID = "productId";
  private static final Order MOCK_ORDER =
      new Order(
          "seller-id",
          USER_ID,
          new ProductDTO(PRODUCT_ID, "Test Product", "Description", 100.0, 1, "seller-id"),
          1);

  private void setUpSecurityContextWithPrincipal(UserDetailsImpl principal) {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication())
        .thenReturn(new UsernamePasswordAuthenticationToken(principal, null));
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenNoOrders_ShouldReturnNull() {
    UserDetailsImpl principal = new UserDetailsImpl(USER_ID, "John Doe", Collections.emptyList());
    when(orderRepository.findAllByBuyerId(USER_ID)).thenReturn(Optional.empty());

    PersonalOrders result = orderService.getOrders(principal);

    assertNull(result);
  }

  @Test
  void whenOneConfirmedOrder_ShouldReturnConfirmedList() {
    UserDetailsImpl principal = new UserDetailsImpl(USER_ID, "John Doe", Collections.emptyList());
    Order order = new Order(USER_ID, OTHER_USER_ID, null, 1);
    order.setStatus(OrderStatus.CONFIRMED);
    when(orderRepository.findAllByBuyerId(USER_ID)).thenReturn(Optional.of(new Order[] {order}));

    PersonalOrders result = orderService.getOrders(principal);

    assertNotNull(result);
    assertEquals(1, result.getConfirmed().length);
    assertEquals(OrderStatus.CONFIRMED, result.getConfirmed()[0].getStatus());
  }

  @Test
  void whenThreeOrdersWithDifferentStatuses_ShouldReturnCorrectLists() {
    UserDetailsImpl principal = new UserDetailsImpl(USER_ID, "John Doe", Collections.emptyList());

    Order pendingOrder = new Order(USER_ID, OTHER_USER_ID, null, 1);
    pendingOrder.setStatus(OrderStatus.PENDING);

    Order confirmedOrder = new Order(USER_ID, OTHER_USER_ID, null, 1);
    confirmedOrder.setStatus(OrderStatus.CONFIRMED);

    Order cancelledOrder = new Order(USER_ID, OTHER_USER_ID, null, 1);
    cancelledOrder.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findAllByBuyerId(USER_ID))
        .thenReturn(Optional.of(new Order[] {pendingOrder, confirmedOrder, cancelledOrder}));

    PersonalOrders result = orderService.getOrders(principal);

    assertNotNull(result);
    assertEquals(1, result.getPending().length);
    assertEquals(OrderStatus.PENDING, result.getPending()[0].getStatus());
    assertEquals(1, result.getConfirmed().length);
    assertEquals(OrderStatus.CONFIRMED, result.getConfirmed()[0].getStatus());
    assertEquals(1, result.getCancelled().length);
    assertEquals(OrderStatus.CANCELLED, result.getCancelled()[0].getStatus());
  }

  @Test
  void handleCartOrder_SuccessWithoutModifications() throws TimeoutException {
    Order order =
        new Order(
            "seller-id",
            USER_ID,
            new ProductDTO(PRODUCT_ID, "Test Product", "Description", 100.0, 1, "seller-id"),
            1);
    Cart cart = new Cart();
    cart.setOrders(new Order[] {order});

    CartValidationResponse response = new CartValidationResponse();
    response.setProcessed(true);
    response.setCart(cart);

    when(kafkaService.sendCartValidationRequestAndWaitForResponse(any())).thenReturn(response);

    CartResponse cartResponse = orderService.handleCartOrder(USER_ID, cart, false);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getProcessed());
    assertNull(cartResponse.getOrderModifications());

    verify(orderRepository, times(1)).save(order);
    verify(cartService, times(1)).deleteItemFromCart(order.getId(), USER_ID);
  }

  @Test
  void handleCartOrder_SuccessWithModifications() throws TimeoutException {
    Order order =
        new Order(
            "seller-id",
            USER_ID,
            new ProductDTO(PRODUCT_ID, "Test Product", "Description", 100.0, 1, "seller-id"),
            1);
    Cart cart = new Cart();
    cart.setOrders(new Order[] {order});

    OrderModifications modifications = new OrderModifications();
    modifications.setModifications(new Order[] {order});

    CartValidationResponse response = new CartValidationResponse();
    response.setProcessed(true);
    response.setCart(cart);
    response.setOrderModifications(modifications);

    when(kafkaService.sendCartValidationRequestAndWaitForResponse(any())).thenReturn(response);

    CartResponse cartResponse = orderService.handleCartOrder(USER_ID, cart, false);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getProcessed());
    assertNotNull(cartResponse.getOrderModifications());

    verify(cartService, times(1)).updateCartContents(new Order[] {order});
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  void handleCartOrder_KafkaServiceTimeout() {
    Cart cart = new Cart();
    cart.setOrders(
        new Order[] {
          new Order(
              "seller-id",
              USER_ID,
              new ProductDTO(PRODUCT_ID, "Test Product", "Description", 100.0, 1, "seller-id"),
              1)
        });

    when(kafkaService.sendCartValidationRequestAndWaitForResponse(any())).thenReturn(null);

    assertThrows(TimeoutException.class, () -> orderService.handleCartOrder(USER_ID, cart, false));

    verify(cartService, never()).deleteItemFromCart(anyString(), anyString());
    verify(cartService, never()).updateCartContents(any());
    verify(orderRepository, never()).save(any());
  }

  @Test
  void placeOrderNotFound() {
    when(cartService.getCart(anyString())).thenReturn(null);

    assertThrows(NotFoundException.class, () -> orderService.placeOrder(USER_ID));
  }

  @Test
  void placeOrder_Successful() throws TimeoutException {
    Cart cart = new Cart();
    cart.setOrders(new Order[] {MOCK_ORDER});
    when(cartService.getCart(USER_ID)).thenReturn(cart);

    CartValidationResponse response = new CartValidationResponse();
    response.setProcessed(true);
    response.setCart(cart);
    when(kafkaService.sendCartValidationRequestAndWaitForResponse(any())).thenReturn(response);

    CartResponse cartResponse = orderService.placeOrder(USER_ID);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getProcessed());
    assertEquals(cart, cartResponse.getCart());

    verify(cartService).getCart(USER_ID);
    verify(kafkaService).sendCartValidationRequestAndWaitForResponse(any());
    verify(orderRepository, times(1)).save(MOCK_ORDER);
  }

  @Test
  void reOrder_OrderNotFound_ThrowsNotFoundException() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> orderService.reOrder(ORDER_ID));
  }

  @Test
  void reOrder_OrderDoesNotBelongToUser_ThrowsForbiddenException() {
    Order order = new Order("seller-id", OTHER_USER_ID, null, 1);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    UserDetailsImpl principal = new UserDetailsImpl(USER_ID, "John Doe", null);
    setUpSecurityContextWithPrincipal(principal);

    assertThrows(ForbiddenException.class, () -> orderService.reOrder(ORDER_ID));
  }

  @Test
  void reOrderSuccessful() throws TimeoutException, NotFoundException, ForbiddenException {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(MOCK_ORDER));

    UserDetailsImpl principal = new UserDetailsImpl(USER_ID, "John Doe", null);
    setUpSecurityContextWithPrincipal(principal);

    Cart cartWithOrders = new Cart();
    Order[] orders =
        new Order[] {
          new Order(
              MOCK_ORDER.getSellerId(),
              MOCK_ORDER.getBuyerId(),
              MOCK_ORDER.getProduct(),
              MOCK_ORDER.getQuantity())
        };
    cartWithOrders.setOrders(orders);

    CartValidationResponse response = new CartValidationResponse();
    response.setProcessed(true);
    response.setCart(cartWithOrders);
    when(kafkaService.sendCartValidationRequestAndWaitForResponse(any())).thenReturn(response);

    CartResponse cartResponse = orderService.reOrder(ORDER_ID);

    assertNotNull(cartResponse);
    assertTrue(cartResponse.getProcessed());
  }

  @Test
  void changeOrderStatus_OrderNotFound() {
    when(orderRepository.findById(anyString())).thenReturn(Optional.empty());
    assertThrows(
        NotFoundException.class,
        () ->
            orderService.changeOrderStatus(
                new OrderStatusUpdate(ORDER_ID, OrderStatus.CONFIRMED), USER_ID, "ROLE_SELLER"));
  }

  @Test
  void changeOrderStatus_UnauthorizedUpdate() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(MOCK_ORDER));
    assertThrows(
        ForbiddenException.class,
        () ->
            orderService.changeOrderStatus(
                new OrderStatusUpdate(ORDER_ID, OrderStatus.PENDING), USER_ID, "ROLE_SELLER"));
  }

  @Test
  void changeOrderStatus_ForbiddenAccess() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(MOCK_ORDER));
    assertThrows(
        ForbiddenException.class,
        () ->
            orderService.changeOrderStatus(
                new OrderStatusUpdate(ORDER_ID, OrderStatus.CONFIRMED),
                "someOtherUserId",
                "ROLE_SELLER"));
  }

  @Test
  void changeOrderStatus_OrderAlreadyProcessed() {
    Order mockOrderWithProcessedStatus =
        new Order(
            MOCK_ORDER.getSellerId(),
            MOCK_ORDER.getBuyerId(),
            MOCK_ORDER.getProduct(),
            MOCK_ORDER.getQuantity());
    mockOrderWithProcessedStatus.setStatus(OrderStatus.CANCELLED);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mockOrderWithProcessedStatus));

    assertThrows(
        ForbiddenException.class,
        () ->
            orderService.changeOrderStatus(
                new OrderStatusUpdate(ORDER_ID, OrderStatus.CONFIRMED), USER_ID, "ROLE_SELLER"));
  }

  @Test
  void changeOrderStatus_SuccessfulUpdate() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(MOCK_ORDER));
    when(orderRepository.save(any(Order.class))).thenReturn(MOCK_ORDER);

    OrderStatusUpdate update = new OrderStatusUpdate(ORDER_ID, OrderStatus.CONFIRMED);
    Order updatedOrder = orderService.changeOrderStatus(update, USER_ID, "ROLE_SELLER");

    assertNotNull(updatedOrder);
    assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStatus());
    verify(kafkaService, never()).sendProductOrderCancellation(any());
  }

  @Test
  void changeOrderStatus_CancelOrder() {
    MOCK_ORDER.setStatus(OrderStatus.PENDING);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(MOCK_ORDER));
    when(orderRepository.save(any(Order.class))).thenReturn(MOCK_ORDER);

    OrderStatusUpdate update = new OrderStatusUpdate(ORDER_ID, OrderStatus.CANCELLED);
    Order updatedOrder = orderService.changeOrderStatus(update, USER_ID, "ROLE_SELLER");

    assertNotNull(updatedOrder);
    assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    verify(kafkaService).sendProductOrderCancellation(any(ProductOrderCancellationMessage.class));
  }
}
