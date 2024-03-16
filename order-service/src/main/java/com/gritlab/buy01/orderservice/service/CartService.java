package com.gritlab.buy01.orderservice.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.dto.ProductDTO;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.model.CartItem;
import com.gritlab.buy01.orderservice.model.Order;
import com.gritlab.buy01.orderservice.repository.CartRepository;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;

@Service
public class CartService {
  private final CartRepository cartRepository;

  public CartService(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  public Cart getCart(String userId) {
    Optional<ArrayList<CartItem>> cartQuery = cartRepository.findAllByBuyerId(userId);

    if (cartQuery.isEmpty()) {
      return null;
    }

    Cart cart = new Cart();
    ArrayList<Order> orders = new ArrayList<>();
    ArrayList<CartItem> cartItems = cartQuery.get();
    for (CartItem item : cartItems) {
      Order order = new Order();
      order.setId(item.getId());
      order.setSellerId(item.getSellerId());
      order.setBuyerId(item.getBuyerId());
      order.setProduct(item.getProduct());
      order.setQuantity(item.getQuantity());

      orders.add(order);
    }
    cart.setOrders(orders.toArray(new Order[0]));

    return cart;
  }

  public CartItem addToCart(CartItemDTO cartItemDTO) {
    Optional<CartItem> cartItemQuery =
        cartRepository.findByBuyerIdAndProductId(
            cartItemDTO.getBuyerId(), cartItemDTO.getProduct().getId());
    if (cartItemQuery.isPresent()) {
      return null;
    }

    CartItem item = new CartItem(cartItemDTO);
    return this.cartRepository.save(item);
  }

  public void updateCartContents(Order[] contents) {
    for (Order content : contents) {
      Optional<CartItem> cartItemQuery = cartRepository.findById(content.getId());
      if (cartItemQuery.isPresent()) {
        CartItem cartItem = cartItemQuery.get();
        ProductDTO contentProduct = content.getProduct();
        cartItem.setSellerId(contentProduct.getUserId());
        cartItem.setProduct(contentProduct);
        if (cartItem.getQuantity() > content.getProduct().getQuantity()) {
          cartItem.setQuantity(content.getProduct().getQuantity());
        }
        cartRepository.save(cartItem);
      }
    }
  }

  public void deleteItemFromCart(String cartItemId, String userId)
      throws ForbiddenException, NotFoundException {
    Optional<CartItem> itemQuery = cartRepository.findById(cartItemId);

    if (itemQuery.isEmpty()) {
      throw new NotFoundException("Error: cart item not found");
    }

    if (!itemQuery.get().getBuyerId().equals(userId)) {
      throw new ForbiddenException("Error: this cartitem is not yours");
    }

    cartRepository.deleteById(cartItemId);
  }

  public void deleteUserCart(String userId) {
    cartRepository.deleteAllByBuyerId(userId);
  }

  public CartItemDTO updateCartItemQuantity(String id, Integer quantity)
      throws ForbiddenException, NotFoundException {
    Optional<CartItem> cartItemQuery = cartRepository.findById(id);
    if (cartItemQuery.isEmpty()) {
      throw new NotFoundException(
          String.format("Error: cart item could not be found by id %s", id));
    }

    CartItem item = cartItemQuery.get();
    UserDetailsImpl principal = UserDetailsImpl.getPrincipal();

    if (!item.getBuyerId().equals(principal.getId())) {
      throw new ForbiddenException("Error: cart item does not belong to you");
    }

    item.setQuantity(quantity);

    cartRepository.save(item);

    return new CartItemDTO(
        item.getId(),
        item.getSellerId(),
        item.getBuyerId(),
        item.getProduct(),
        item.getQuantity(),
        item.getAddedToCartAt());
  }
}
