package com.gritlab.buy01.orderservice.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartResponse;
import com.gritlab.buy01.orderservice.dto.OrderModifications;
import com.gritlab.buy01.orderservice.dto.OrderStatusUpdate;
import com.gritlab.buy01.orderservice.dto.PersonalOrders;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.kafka.message.CartValidationRequest;
import com.gritlab.buy01.orderservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.orderservice.kafka.message.ProductOrderCancellationMessage;
import com.gritlab.buy01.orderservice.model.Order;
import com.gritlab.buy01.orderservice.model.enums.OrderStatus;
import com.gritlab.buy01.orderservice.repository.OrderRepository;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;

@Service
public class OrderService {
  private final OrderRepository orderRepository;
  private final KafkaService kafkaService;
  private final CartService cartService;

  @Autowired
  public OrderService(
      OrderRepository orderRepository, KafkaService kafkaService, CartService cartService) {
    this.orderRepository = orderRepository;
    this.kafkaService = kafkaService;
    this.cartService = cartService;
  }

  public PersonalOrders getOrders(UserDetailsImpl principal) {
    Optional<Order[]> ordersQuery;
    if (principal.hasRole("ROLE_SELLER")) {
      ordersQuery = orderRepository.findAllBySellerId(principal.getId());
    } else {
      ordersQuery = orderRepository.findAllByBuyerId(principal.getId());
    }

    if (ordersQuery.isEmpty()) {
      return null;
    }

    Order[] orders = ordersQuery.get();
    ArrayList<Order> pending = new ArrayList<>();
    ArrayList<Order> confirmed = new ArrayList<>();
    ArrayList<Order> cancelled = new ArrayList<>();

    for (Order order : orders) {
      switch (order.getStatus()) {
        case PENDING:
          pending.add(order);
          break;
        case CONFIRMED:
          confirmed.add(order);
          break;
        case CANCELLED:
          cancelled.add(order);
      }
    }
    return new PersonalOrders(
        pending.toArray(new Order[0]),
        confirmed.toArray(new Order[0]),
        cancelled.toArray(new Order[0]));
  }

  public CartResponse handleCartOrder(String userId, Cart cart, boolean reorder)
      throws TimeoutException {
    System.out.println("userId: " + userId + " cart:" + cart + " reorder:" + reorder);
    CartValidationRequest cartValidationRequest = new CartValidationRequest();
    cartValidationRequest.setCorrelationId(UUID.randomUUID().toString());
    cartValidationRequest.setCart(cart);

    CartValidationResponse cartValidationResponse =
        kafkaService.sendCartValidationRequestAndWaitForResponse(cartValidationRequest);

    if (cartValidationResponse == null) {
      throw new TimeoutException("Error: Did not receive response from product-service");
    }

    OrderModifications modifications = cartValidationResponse.getOrderModifications();
    if (modifications != null) {
      cartService.updateCartContents(modifications.getModifications());
    }

    if (cartValidationResponse.isProcessed()) {
      for (Order order : cartValidationResponse.getCart().getOrders()) {
        orderRepository.save(order);
        if (!reorder) cartService.deleteItemFromCart(order.getId(), userId);
      }
    }

    return new CartResponse(
        cartValidationResponse.getCart(),
        cartValidationResponse.isProcessed(),
        cartValidationResponse.getOrderModifications());
  }

  public CartResponse placeOrder(String userId) throws TimeoutException, NotFoundException {
    Cart cart = cartService.getCart(userId);
    if (cart == null) {
      throw new NotFoundException("Error: could not find cart");
    }

    return handleCartOrder(userId, cart, false);
  }

  public CartResponse reOrder(String orderId)
      throws NotFoundException, ForbiddenException, TimeoutException {
    System.out.println("orderId in reOrder: " + orderId);
    Optional<Order> orderQuery = orderRepository.findById(orderId);
    if (orderQuery.isEmpty()) {
      throw new NotFoundException("Error: order not found");
    }
    Order order = orderQuery.get();
    System.out.println("order in reOrder: " + order);

    UserDetailsImpl principal = UserDetailsImpl.getPrincipal();
    if (!principal.getId().equals(order.getBuyerId())) {
      throw new ForbiddenException("Error: not your order");
    }

    order.setId(null);
    Cart cart = new Cart();
    cart.setOrders(new Order[] {order});
    return handleCartOrder(principal.getId(), cart, true);
  }

  public Order changeOrderStatus(OrderStatusUpdate update, String userId, String role)
      throws NotFoundException, ForbiddenException {
    Optional<Order> orderQuery = orderRepository.findById(update.getId());
    if (orderQuery.isEmpty()) {
      throw new NotFoundException("Error: order not found");
    }

    if (update.getStatus() == OrderStatus.PENDING
        || (role.equals("ROLE_CLIENT") && update.getStatus() != OrderStatus.CANCELLED)) {
      throw new ForbiddenException("Error: unauthorized status update");
    }

    Order order = orderQuery.get();

    if (!order.getSellerId().equals(userId) && !order.getBuyerId().equals(userId)) {
      throw new ForbiddenException("Error: you are neither the seller, nor the buyer");
    }

    if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.CONFIRMED) {
      String message =
          String.format("Error: order is already %s", order.getStatus().toString().toLowerCase());
      throw new ForbiddenException(message);
    }

    order.setStatus(update.getStatus());

    if (order.getStatus() == OrderStatus.CANCELLED) {
      ProductOrderCancellationMessage message = new ProductOrderCancellationMessage();
      message.setCorrelationId(UUID.randomUUID().toString());
      message.setProductId(order.getProduct().getId());
      message.setQuantity(order.getQuantity());

      kafkaService.sendProductOrderCancellation(message);
    }

    return orderRepository.save(order);
  }

}
