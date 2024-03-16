package com.gritlab.buy01.productservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ProductModelTest {

  private ProductModel product;
  private Validator validator;

  @BeforeEach
  void setUp() {
    product = new ProductModel();
    product.setName("ValidName");
    product.setDescription("ValidDescription");
    product.setPrice(10.0);
    product.setQuantity(5);
    product.setUserId("ValidUserId");

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testNoArgsConstructor() {
    ProductModel noArgsProduct = new ProductModel();
    assertNotNull(noArgsProduct);
    assertNull(noArgsProduct.getName());
    assertNull(noArgsProduct.getDescription());
    assertNull(noArgsProduct.getPrice());
    assertNull(noArgsProduct.getQuantity());
    assertNull(noArgsProduct.getUserId());
  }

  @Test
  void testAllArgsConstructor() {
    ProductModel productWithArgs =
        new ProductModel("TestName", "TestDescription", 10.5, 5, "UserId123");
    assertEquals("TestName", productWithArgs.getName());
    assertEquals("TestDescription", productWithArgs.getDescription());
    assertEquals(10.5, productWithArgs.getPrice());
    assertEquals(5, productWithArgs.getQuantity());
    assertEquals("UserId123", productWithArgs.getUserId());
  }

  @Test
  void testNameSizeValidation() {
    product.setName("Te"); // less than 3 characters
    Set<ConstraintViolation<ProductModel>> violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals(
        "Name has to be between 3 and 50 characters long",
        violations.iterator().next().getMessage());

    product.setName("T".repeat(51)); // more than 50 characters
    violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals(
        "Name has to be between 3 and 50 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void testDescriptionSizeValidation() {
    product.setDescription("Te"); // less than 3 characters
    Set<ConstraintViolation<ProductModel>> violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals(
        "Description has to be between 3 and 300 characters long",
        violations.iterator().next().getMessage());

    product.setDescription("T".repeat(320)); // more than 300 characters
    violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals(
        "Description has to be between 3 and 300 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void testPriceValidation() {
    product.setPrice(-1.0); // less than 0
    Set<ConstraintViolation<ProductModel>> violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals(
        "Price must be a positive value or zero", violations.iterator().next().getMessage());
  }

  @Test
  void testQuantityValidation() {
    product.setQuantity(-1); // less than 0
    Set<ConstraintViolation<ProductModel>> violations = validator.validate(product);
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertEquals("Quantity cannot be less than 0", violations.iterator().next().getMessage());
  }
}
