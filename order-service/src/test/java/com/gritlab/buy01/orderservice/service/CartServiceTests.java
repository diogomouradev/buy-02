package com.gritlab.buy01.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gritlab.buy01.orderservice.dto.Cart;
import com.gritlab.buy01.orderservice.dto.CartItemDTO;
import com.gritlab.buy01.orderservice.dto.ProductDTO;
import com.gritlab.buy01.orderservice.exception.ForbiddenException;
import com.gritlab.buy01.orderservice.exception.NotFoundException;
import com.gritlab.buy01.orderservice.model.CartItem;
import com.gritlab.buy01.orderservice.model.Order;
import com.gritlab.buy01.orderservice.repository.CartRepository;
import com.gritlab.buy01.orderservice.security.UserDetailsImpl;

class CartServiceTests {
  private static final String USER_ID = "userId";
  private static final String SELLER_ID = "sellerId";
  private static final String CART_ITEM_ID = "cartItemId";

  private ProductDTO mockProduct;
  private CartItem cartItem;

  @Mock private CartRepository cartRepository;
  @InjectMocks private CartService cartService;

  @Captor private ArgumentCaptor<CartItem> cartItemCaptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    initializeTestData();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void initializeTestData() {
    mockProduct =
        new ProductDTO("productId", "productName", "productDescription", 123.23, 1, SELLER_ID);
    cartItem = new CartItem(CART_ITEM_ID, SELLER_ID, USER_ID, mockProduct, 1, new Date());
  }

  @Test
  void testGetCartEmptyCart() {
    when(cartRepository.findAllByBuyerId(USER_ID)).thenReturn(Optional.empty());

    Cart cart = cartService.getCart(USER_ID);
    assertNull(cart);
  }

  @Test
  void testGetCartNotEmpty() {
    ArrayList<CartItem> mockList = new ArrayList<>();
    mockList.add(cartItem);

    when(cartRepository.findAllByBuyerId(USER_ID)).thenReturn(Optional.of(mockList));

    Cart cart = cartService.getCart(USER_ID);

    assertNotNull(cart);
    assertNotNull(cart.getOrders());
    assertEquals(1, cart.getOrders().length);
    Order resultOrder = cart.getOrders()[0];
    assertEquals(SELLER_ID, resultOrder.getSellerId());
    assertEquals(USER_ID, resultOrder.getBuyerId());
    assertEquals(mockProduct, resultOrder.getProduct());
    assertEquals(1, resultOrder.getQuantity());
  }

  @Test
  void testAddToCartAlreadyInCart() {
    CartItemDTO mockDTO =
        new CartItemDTO(
            cartItem.getSellerId(),
            cartItem.getBuyerId(),
            cartItem.getProduct(),
            cartItem.getQuantity());
    when(cartRepository.findByBuyerIdAndProductId(
            mockDTO.getBuyerId(), mockDTO.getProduct().getId()))
        .thenReturn(Optional.of(cartItem));

    CartItem item = cartService.addToCart(mockDTO);
    assertNull(item);
  }

  @Test
  void testAddToCartSuccess() {
    CartItemDTO mockDTO =
        new CartItemDTO(
            cartItem.getSellerId(),
            cartItem.getBuyerId(),
            cartItem.getProduct(),
            cartItem.getQuantity());
    when(cartRepository.findByBuyerIdAndProductId(
            mockDTO.getBuyerId(), mockDTO.getProduct().getId()))
        .thenReturn(Optional.empty());

    when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);

    CartItem cartItemRet = cartService.addToCart(mockDTO);

    assertNotNull(cartItemRet);
    assertEquals(cartItem, cartItemRet);
  }

  @Test
  void testUpdateCartContentsSuccess() {
    String updatedSellerId = "updatedSellerId";
    ProductDTO updatedProduct =
        new ProductDTO(
            "updatedProductId",
            "Updated Product",
            "Updated Description",
            200.00,
            2,
            updatedSellerId);
    Order updatedOrder = new Order("updatedSellerId", USER_ID, updatedProduct, 2);
    updatedOrder.setId(cartItem.getId());
    Order[] updatedOrders = new Order[] {updatedOrder};

    when(cartRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

    cartService.updateCartContents(updatedOrders);

    verify(cartRepository).findById(CART_ITEM_ID);
    verify(cartRepository).save(cartItemCaptor.capture());

    CartItem updatedCartItem = cartItemCaptor.getValue();
    assertEquals(updatedSellerId, updatedCartItem.getSellerId());
    assertEquals(updatedProduct, updatedCartItem.getProduct());
    assertEquals(1, updatedCartItem.getQuantity());
  }

  @Test
  void testDeleteFromCartNotFound() {
    when(cartRepository.findById(any(String.class))).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> cartService.deleteItemFromCart(CART_ITEM_ID, USER_ID));
  }

  @Test
  void testDeleteFromCartForbidden() {
    String otherItemId = "itemId";
    CartItem other = new CartItem(otherItemId, SELLER_ID, "otherBuyer", mockProduct, 1, new Date());

    when(cartRepository.findById(otherItemId)).thenReturn(Optional.of(other));

    assertThrows(
        ForbiddenException.class, () -> cartService.deleteItemFromCart(otherItemId, USER_ID));
  }

  @Test
  void testDeleteFromCartSuccess() {
    when(cartRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

    assertDoesNotThrow(() -> cartService.deleteItemFromCart(CART_ITEM_ID, USER_ID));
  }

  @Test
  void testUpdateCartItemQuantityNotFound() {
    when(cartRepository.findById(any(String.class))).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> cartService.updateCartItemQuantity(CART_ITEM_ID, 1));
  }

  @Test
  void testUpdateCartItemQuantityForbidden() {
    when(cartRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(cartItem));

    List<GrantedAuthority> mockAuthorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"));
    UserDetailsImpl mockUserDetails = new UserDetailsImpl("someId", "John Doe", mockAuthorities);

    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);

    assertThrows(
        ForbiddenException.class, () -> cartService.updateCartItemQuantity(CART_ITEM_ID, 1));
  }

  @Test
  void testUpdateCartItemQuantitySuccess() {
    when(cartRepository.findById(any(String.class))).thenReturn(Optional.of(cartItem));

    List<GrantedAuthority> mockAuthorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"));
    UserDetailsImpl mockUserDetails = new UserDetailsImpl(USER_ID, "John Doe", mockAuthorities);

    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(mockUserDetails);

    when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);

    Integer updatedQuantity = 123;

    cartService.updateCartItemQuantity(USER_ID, updatedQuantity);

    verify(cartRepository).save(cartItemCaptor.capture());
    CartItem savedCartItem = cartItemCaptor.getValue();

    assertEquals(updatedQuantity, savedCartItem.getQuantity());
  }
}
