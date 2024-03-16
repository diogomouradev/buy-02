package com.gritlab.buy01.orderservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
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

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.dto.ProductDTO;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.UnexpectedPrincipalTypeException;
import com.gritlab.buy01.orderservice.model.CartItem;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;
import com.gritlab.buy01.orderservice.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {
  private static final String USER_ID = "userId";
  private static final String SELLER_ID = "sellerId";

  @Mock private CartService cartService;

  @Mock private UserDetailsImpl mockUserDetails;

  @InjectMocks private CartController cartController;

  @BeforeEach
  void setUp() {
    mockUserDetails =
        new UserDetailsImpl(
            USER_ID,
            "userName",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT")));

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient()
        .when(securityContext.getAuthentication())
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                mockUserDetails, null, mockUserDetails.getAuthorities()));
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testGetCartSuccess() {
    Cart mockCart = new Cart();
    when(cartService.getCart(anyString())).thenReturn(mockCart);

    ResponseEntity<Object> response = cartController.getCart();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockCart, response.getBody());
  }

  @Test
  void testGetCartNoContent() {
    when(cartService.getCart(anyString())).thenReturn(null);

    ResponseEntity<Object> response = cartController.getCart();
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void testGetCartWithUnexpectedPrincipalTypeException() {
    when(cartService.getCart(anyString()))
        .thenThrow(new UnexpectedPrincipalTypeException("Unexpected principal type"));

    ResponseEntity<Object> response = cartController.getCart();
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testAddItemToCartWithForbiddenException() {
    CartItemDTO mockItemDTO = new CartItemDTO();
    ProductDTO mockProduct =
        new ProductDTO("productId", "productName", "productDescription", 10.0, 5, SELLER_ID);
    mockItemDTO.setBuyerId(USER_ID);
    mockItemDTO.setSellerId(SELLER_ID);
    mockItemDTO.setProduct(mockProduct);
    mockItemDTO.setQuantity(1);
    when(cartService.addToCart(any(CartItemDTO.class)))
        .thenThrow(new ForbiddenException("Only clients can add items"));

    ResponseEntity<Object> response = cartController.addItemToCart(mockItemDTO);
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
  }

  @Test
  void testAddItemToCartSuccess() {
    CartItemDTO mockItemDTO = new CartItemDTO();
    ProductDTO mockProduct =
        new ProductDTO("productId", "productName", "productDescription", 10.0, 5, SELLER_ID);
    mockItemDTO.setBuyerId(USER_ID);
    mockItemDTO.setSellerId(SELLER_ID);
    mockItemDTO.setProduct(mockProduct);
    mockItemDTO.setQuantity(1);

    CartItem mockCartItem = new CartItem();
    when(cartService.addToCart(any(CartItemDTO.class))).thenReturn(mockCartItem);

    ResponseEntity<Object> response = cartController.addItemToCart(mockItemDTO);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockCartItem, response.getBody());
  }

  @Test
  void testDeleteItemFromCartSuccess() {
    doNothing().when(cartService).deleteItemFromCart(anyString(), anyString());

    ResponseEntity<Object> response = cartController.deleteItemFromCart("1");
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void testUpdateCartItemQuantitySuccess() {
    CartItemDTO mockCartItemDTO = new CartItemDTO();
    when(cartService.updateCartItemQuantity(anyString(), anyInt())).thenReturn(mockCartItemDTO);

    ResponseEntity<Object> response = cartController.updateCartItemQuantity("1", 10);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(mockCartItemDTO, response.getBody());
  }
}
