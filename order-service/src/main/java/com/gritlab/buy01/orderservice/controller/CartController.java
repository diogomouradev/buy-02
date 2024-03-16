package com.gritlab.buy01.orderservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.exception.UnexpectedPrincipalTypeException;
import com.gritlab.buy01.orderservice.model.CartItem;
import com.gritlab.buy01.orderservice.payload.response.ErrorMessage;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;
import com.gritlab.buy01.orderservice.service.CartService;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class CartController {
  private final CartService cartService;

  private static final String CLIENT = "ROLE_CLIENT";

  @Autowired
  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/carts")
  public ResponseEntity<Object> getCart() {
    try {
      UserDetailsImpl principal = UserDetailsImpl.getPrincipal();
      Cart cart = cartService.getCart(principal.getId());
      if (cart == null) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(cart, HttpStatus.OK);

    } catch (UnexpectedPrincipalTypeException | ForbiddenException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

    } catch (Exception e) {

      ErrorMessage error =
          new ErrorMessage(
              "Error: something went wrong with fetching the cart",
              HttpStatus.INTERNAL_SERVER_ERROR.value());
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/carts/add")
  public ResponseEntity<Object> addItemToCart(@RequestBody CartItemDTO item) {
    try {
      if (item.getQuantity() <= 0) {
        throw new ForbiddenException("Error: quantity has to be more than zero");
      }

      UserDetailsImpl principal = UserDetailsImpl.getPrincipal();
      if (!principal.hasRole(CLIENT)) {
        throw new ForbiddenException("Error: only clients can add items to cart");
      }

      if (!principal.getId().equals(item.getBuyerId())) {
        throw new ForbiddenException("Error: you can only add items to your own cart");
      }

      CartItem addedToCart = this.cartService.addToCart(item);

      // return will be the new added CartItem, or null if the product is already in the user's cart
      return new ResponseEntity<>(addedToCart, HttpStatus.OK);

    } catch (ForbiddenException | UnexpectedPrincipalTypeException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

    } catch (Exception e) {

      ErrorMessage error =
          new ErrorMessage(
              "Error: something went wrong with adding product to cart: " + e.getMessage(),
              HttpStatus.INTERNAL_SERVER_ERROR.value());
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/carts/{id}")
  public ResponseEntity<Object> deleteItemFromCart(@PathVariable("id") String id) {
    try {
      UserDetailsImpl principal = UserDetailsImpl.getPrincipal();
      if (!principal.hasRole(CLIENT)) {
        throw new ForbiddenException("Error: only clients have a cart");
      }

      this.cartService.deleteItemFromCart(id, principal.getId());
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    } catch (ForbiddenException | UnexpectedPrincipalTypeException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

    } catch (NotFoundException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.NOT_FOUND.value());
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

    } catch (Exception e) {

      ErrorMessage error =
          new ErrorMessage(
              "Error: something went wrong with removing item from cart",
              HttpStatus.INTERNAL_SERVER_ERROR.value());
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/carts")
  public ResponseEntity<Object> updateCartItemQuantity(
      @RequestParam(required = true) String id, @RequestParam(required = true) Integer quantity) {
    try {

      CartItemDTO updatedCart = cartService.updateCartItemQuantity(id, quantity);
      return new ResponseEntity<>(updatedCart, HttpStatus.OK);

    } catch (ForbiddenException | UnexpectedPrincipalTypeException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

    } catch (NotFoundException e) {

      ErrorMessage error = new ErrorMessage(e.getMessage(), HttpStatus.NOT_FOUND.value());
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

    } catch (Exception e) {

      ErrorMessage error =
          new ErrorMessage(
              "Error: something went wrong with item quantity update",
              HttpStatus.INTERNAL_SERVER_ERROR.value());
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
