package com.gritlab.buy01.productservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gritlab.buy01.productservice.model.ProductDTO;
import com.gritlab.buy01.productservice.model.ProductModel;
import com.gritlab.buy01.productservice.repository.ProductRepository;

public class ProductServiceTests {

  @InjectMocks private ProductService productService;

  @Mock private ProductRepository productRepository;

  @Mock private KafkaService kafkaService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllProducts() {
    when(productRepository.findAll()).thenReturn(Arrays.asList(new ProductModel()));
    assertEquals(1, productService.getAllProducts(null).size());
  }

  @Test
  void testGetProductByName() {
    when(productRepository.findByName("testName")).thenReturn(Arrays.asList(new ProductModel()));
    assertEquals(1, productService.getAllProducts("testName").size());
  }

  @Test
  void testGetProductById() {
    when(productRepository.findById("123")).thenReturn(Optional.of(new ProductModel()));
    assertTrue(productService.getProductById("123").isPresent());
  }

  @Test
  void testGetAllProductsByUserId() {
    when(productRepository.findAllByUserId("456")).thenReturn(Arrays.asList(new ProductModel()));
    assertEquals(1, productService.getAllProductsByUserId("456").size());
  }

  @Test
  void testCreateProduct() {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setName("test");
    productDTO.setDescription("desc");
    productDTO.setPrice(100.0);
    productDTO.setQuantity(1);
    productDTO.setUserId("789");

    ProductModel productModel = new ProductModel("test", "desc", 100.0, 1, "789");

    when(productRepository.save(any())).thenReturn(productModel);

    ProductModel savedProduct = productService.createProduct(productDTO);
    assertEquals("test", savedProduct.getName());
  }

  @Test
  void testUpdateProduct() {
    ProductModel productModel = new ProductModel("test", "desc", 100.0, 1, "789");
    when(productRepository.findById("123")).thenReturn(Optional.of(productModel));
    when(productRepository.save(any())).thenReturn(productModel);

    Optional<ProductModel> updatedProduct = productService.updateProduct("123", productModel);
    assertTrue(updatedProduct.isPresent());
    assertEquals("test", updatedProduct.get().getName());
  }

  @Test
  void testDeleteProduct() {
    doNothing().when(productRepository).deleteById("123");
    productService.deleteProduct("123");
    verify(productRepository, times(1)).deleteById("123");
  }

  @Test
  void testDeleteAllUserProducts() {
    ProductModel productModel = new ProductModel("test", "desc", 100.0, 1, "789");
    productModel.setId("123"); // Set the id field of the ProductModel object
    when(productRepository.findAllByUserId("789")).thenReturn(Arrays.asList(productModel));
    doNothing().when(productRepository).deleteById("123");
    doNothing().when(kafkaService).deleteProductMedia("123");

    productService.deleteAllUserProducts("789");
    verify(productRepository, times(1)).deleteById("123");
    verify(kafkaService, times(1)).deleteProductMedia("123");
  }
}
