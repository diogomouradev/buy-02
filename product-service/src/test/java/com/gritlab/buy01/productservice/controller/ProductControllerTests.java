package com.gritlab.buy01.productservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import com.gritlab.buy01.productservice.model.ProductDTO;
import com.gritlab.buy01.productservice.model.ProductModel;
import com.gritlab.buy01.productservice.security.UserDetailsImpl;
import com.gritlab.buy01.productservice.service.ProductService;

public class ProductControllerTests {

  @Mock private ProductService productService;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private ProductController productController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private void simulateSecurityContext(String userId, String name, String... roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
    }
    UserDetailsImpl userDetails = new UserDetailsImpl(userId, name, authorities);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Test
  public void testGetAllProducts() {
    ProductModel product = new ProductModel();
    product.setName("testProduct");
    when(productService.getAllProducts(anyString())).thenReturn(Collections.singletonList(product));

    ResponseEntity<List<ProductModel>> response = productController.getAllProducts("testProduct");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetAllProductsNoContent() {
    when(productService.getAllProducts(anyString())).thenReturn(Collections.emptyList());

    ResponseEntity<List<ProductModel>> response = productController.getAllProducts("testProduct");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  public void testCreateProduct() {
    simulateSecurityContext("123", "testSeller", "SELLER");

    ProductDTO productDTO = new ProductDTO();
    productDTO.setName("testProduct");

    ProductModel productModel = new ProductModel();
    productModel.setName("testProduct");
    when(productService.createProduct(any(ProductDTO.class))).thenReturn(productModel);

    ResponseEntity<?> response = productController.createProduct(productDTO, null);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }

  @Test
  public void testGetProductById() {
    ProductModel product = new ProductModel();
    product.setName("testProduct");
    when(productService.getProductById(anyString())).thenReturn(Optional.of(product));

    ResponseEntity<?> response = productController.getProductById("123");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testGetProductByIdNotFound() {
    when(productService.getProductById(anyString())).thenReturn(Optional.empty());

    ResponseEntity<?> response = productController.getProductById("123");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testGetProductsByUserId() {
    ProductModel product = new ProductModel();
    product.setName("testProduct");
    when(productService.getAllProductsByUserId(anyString()))
        .thenReturn(Collections.singletonList(product));

    ResponseEntity<List<ProductModel>> response = productController.getProductsByUserId("123");

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testUpdateProduct() {
    ProductDTO product = new ProductDTO();
    product.setName("testProduct");
    product.setUserId("123");
    ProductModel productModel = new ProductModel();
    productModel.setName(product.getName());
    productModel.setUserId(product.getUserId());

    when(productService.getProductById(anyString())).thenReturn(Optional.of(productModel));
    when(productService.updateProduct(anyString(), any(ProductDTO.class)))
        .thenReturn(Optional.of(productModel));

    simulateSecurityContext("123", "testUser", "SELLER");

    ResponseEntity<?> response = productController.updateProduct("123", product);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testDeleteProduct() {
    ProductModel product = new ProductModel();
    product.setName("testProduct");
    product.setUserId("123");
    when(productService.getProductById(anyString())).thenReturn(Optional.of(product));

    simulateSecurityContext("123", "testUser", "SELLER");

    ResponseEntity<?> response = productController.deleteProduct("123");

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }
}
