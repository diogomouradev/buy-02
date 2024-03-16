package com.gritlab.buy01.productservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ProductDTOTests {

  private ProductDTO productDTO;
  private Validator validator;

  @BeforeEach
  void setUp() {
    productDTO = new ProductDTO();
    productDTO.setName("ValidName");
    productDTO.setDescription("ValidDescription");
    productDTO.setPrice(10.0);
    productDTO.setQuantity(5);
    productDTO.setUserId("ValidUserId");

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testNameSizeValidation() {
    productDTO.setName("Te");
    Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Name has to be between 3 and 50 characters long",
        violations.iterator().next().getMessage());

    productDTO.setName("T".repeat(51));
    violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Name has to be between 3 and 50 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void testDescriptionSizeValidation() {
    productDTO.setDescription("Te");
    Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Description has to be between 3 and 300 characters long",
        violations.iterator().next().getMessage());

    productDTO.setDescription("T".repeat(301));
    violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Description has to be between 3 and 300 characters long",
        violations.iterator().next().getMessage());
  }

  @Test
  void testPriceValidation() {
    productDTO.setPrice(-1.0);
    Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals(
        "Price must be a positive value or zero", violations.iterator().next().getMessage());
  }

  @Test
  void testQuantityValidation() {
    productDTO.setQuantity(-1);
    Set<ConstraintViolation<ProductDTO>> violations = validator.validate(productDTO);
    assertFalse(violations.isEmpty());
    assertEquals("Quantity cannot be less than 0", violations.iterator().next().getMessage());
  }
}
