package com.gritlab.buy01.productservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gritlab.buy01.productservice.model.ProductDTO;
import com.gritlab.buy01.productservice.model.ProductModel;
import com.gritlab.buy01.productservice.payload.response.ErrorMessage;
import com.gritlab.buy01.productservice.payload.response.ProductCreationResponse;
import com.gritlab.buy01.productservice.security.UserDetailsImpl;
import com.gritlab.buy01.productservice.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ProductController {
  @Autowired ProductService productService;

  @GetMapping("/products")
  public ResponseEntity<List<ProductModel>> getAllProducts(
      @RequestParam(required = false) String name) {
    try {
      List<ProductModel> products = productService.getAllProducts(name);

      if (products.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(products, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/products/{id}")
  public ResponseEntity<?> getProductById(@PathVariable("id") String id) {
    Optional<ProductModel> productData = productService.getProductById(id);

    if (productData.isPresent()) {
      return new ResponseEntity<>(productData.get(), HttpStatus.OK);
    } else {
      ErrorMessage error =
          new ErrorMessage(
              HttpStatus.NOT_FOUND.value(),
              String.format("Product by id %s couldn't be found", id));
      return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/products/user/{userId}")
  public ResponseEntity<List<ProductModel>> getProductsByUserId(
      @PathVariable("userId") String userId) {
    List<ProductModel> usersProducts = productService.getAllProductsByUserId(userId);
    if (usersProducts.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    return new ResponseEntity<>(usersProducts, HttpStatus.OK);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/products")
  public ResponseEntity<?> createProduct(
      @Valid @RequestBody ProductDTO productModel, HttpServletRequest request) {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String userId = null;

      if (principal instanceof UserDetailsImpl) {
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        userId = userDetails.getId();

        // check if the user is ADMIN or SELLER to create product
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
            && !userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SELLER"))) {
          ErrorMessage errorMessage =
              new ErrorMessage(
                  HttpStatus.FORBIDDEN.value(),
                  "Error: only admins and sellers can create products.");
          return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
        }
      } else {
        throw new Exception("Unexpected principal type");
      }
      productModel.setUserId(userId);

      ProductModel _productModel = productService.createProduct(productModel);

      ProductCreationResponse productCreationResponse = new ProductCreationResponse();
      productCreationResponse.setProduct(_productModel);
      productCreationResponse.setErrors(new ArrayList<>());

      return new ResponseEntity<>(productCreationResponse, HttpStatus.CREATED);
    } catch (Exception e) {
      System.out.println(e.toString());
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/products/{id}")
  public ResponseEntity<?> updateProduct(
      @PathVariable("id") String id, @Valid @RequestBody ProductDTO productModel) {
    try {
      Optional<ProductModel> existingProduct = productService.getProductById(id);
      if (existingProduct.isEmpty()) {
        ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Product not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
      }
      ProductModel productToUpdate = existingProduct.get();
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String userId = null;

      if (principal instanceof UserDetailsImpl) {
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        userId = userDetails.getId();

        // check if the user is ADMIN or the owner to update the product
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
            && !userId.equals(productToUpdate.getUserId())) {
          ErrorMessage errorMessage =
              new ErrorMessage(
                  HttpStatus.FORBIDDEN.value(),
                  "Error: only admins and the owner can update a product.");
          return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
        }
      } else {
        throw new Exception("Unexpected principal type");
      }

      Optional<ProductModel> updatedProduct = productService.updateProduct(id, productModel);

      if (updatedProduct.isPresent()) {
        return new ResponseEntity<>(updatedProduct.get(), HttpStatus.OK);
      } else {
        ErrorMessage error =
            new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Product update failed.");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      System.out.println(e);
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("products/{id}")
  public ResponseEntity<?> deleteProduct(@PathVariable String id) {
    try {
      Optional<ProductModel> product = productService.getProductById(id);
      if (product.isEmpty()) {
        ErrorMessage error = new ErrorMessage(HttpStatus.NOT_FOUND.value(), "Product not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
      }
      ProductModel productToDelete = product.get();

      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String userId = null;

      if (principal instanceof UserDetailsImpl) {
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        userId = userDetails.getId();

        // check if the user is ADMIN or the owner to delete the product
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
            && !userId.equals(productToDelete.getUserId())) {
          ErrorMessage errorMessage =
              new ErrorMessage(
                  HttpStatus.FORBIDDEN.value(),
                  "Error: only admins and the owner can delete a product.");
          return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
        }
      } else {
        throw new Exception("Unexpected principal type");
      }
      productService.deleteProduct(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
