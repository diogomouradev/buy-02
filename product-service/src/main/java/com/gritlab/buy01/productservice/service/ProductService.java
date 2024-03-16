package com.gritlab.buy01.productservice.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.productservice.dto.Cart;
import com.gritlab.buy01.productservice.dto.OrderModifications;
import com.gritlab.buy01.productservice.event.CartValidationEvent;
import com.gritlab.buy01.productservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.productservice.model.Order;
import com.gritlab.buy01.productservice.model.ProductDTO;
import com.gritlab.buy01.productservice.model.ProductModel;
import com.gritlab.buy01.productservice.model.enums.OrderStatus;
import com.gritlab.buy01.productservice.repository.ProductRepository;

@Service
public class ProductService {

  public final ProductRepository productRepository;

  private final KafkaService kafkaService;

  private BlockingQueue<CartValidationEvent> eventQueue = new LinkedBlockingQueue<>();

  @Autowired
  public ProductService(ProductRepository productRepository, KafkaService kafkaService) {
    this.productRepository = productRepository;
    this.kafkaService = kafkaService;
  }

  public void enqueueEvent(CartValidationEvent event) {
    eventQueue.add(event);
  }

  private volatile boolean shouldProcessEvents = true;

  public void processEventsSequentially() {
    while (shouldProcessEvents) {
      try {
        CartValidationEvent event = eventQueue.take();

        CartValidationResponse response = new CartValidationResponse();
        response.setCorrelationId(event.getCorrelationId());

        Cart cart = event.getCart();
        response.setCart(cart);

        OrderModifications processedCart = processCart(cart);

        if (processedCart == null) {
          response.setProcessed(true);
        } else {
          response.setProcessed(false);
          response.setOrderModifications(processedCart);
        }
        kafkaService.sendCartValidationResponse(response);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        shouldProcessEvents = false;
      }
    }
  }

  public void stopProcessingEvents() {
    shouldProcessEvents = false;
  }

  public OrderModifications processCart(Cart cart) {
    OrderModifications modifications = new OrderModifications();
    modifications.setNotes(new HashSet<>());
    ArrayList<Order> modList = new ArrayList<>();

    for (Order order : cart.getOrders()) {
      ProductDTO orderedProduct = order.getProduct();
      Optional<ProductModel> productQuery = productRepository.findById(orderedProduct.getId());
      Order possibleMod = new Order();
      possibleMod.setProduct(new ProductDTO());
      possibleMod.getProduct().setId(order.getProduct().getId());

      if (productQuery.isEmpty()) {
        modList.add(possibleMod);
        modifications.getNotes().add("Some order(s) could not be found");
        continue;
      }

      ProductModel product = productQuery.get();
      if (!product.getName().equals(orderedProduct.getName())
          || !product.getDescription().equals(orderedProduct.getDescription())
          || !product.getPrice().equals(orderedProduct.getPrice())
          || !product.getUserId().equals(order.getSellerId())
          || product.getQuantity() < order.getQuantity()) {
        possibleMod.setId(order.getId());
        possibleMod.setSellerId(order.getSellerId());
        possibleMod.setBuyerId(order.getBuyerId());
        possibleMod.setQuantity(order.getQuantity());

        possibleMod.getProduct().setName(product.getName());
        possibleMod.getProduct().setDescription(product.getDescription());
        possibleMod.getProduct().setPrice(product.getPrice());
        possibleMod.getProduct().setQuantity(product.getQuantity());
        possibleMod.getProduct().setUserId(product.getUserId());

        modList.add(possibleMod);

        modifications.getNotes().add("Some order details have changed");
        if (product.getQuantity() < order.getQuantity()) {
          modifications.getNotes().add("Some products were not available in the ordered quantity");
        }
      }
    }

    if (modList.isEmpty()) {
      for (Order order : cart.getOrders()) {
        reduceProductQuantity(order.getProduct().getId(), order.getQuantity());
        order.setOrderPlacedAt(new Date());
        order.setStatus(OrderStatus.PENDING);
      }
      return null;
    }

    modifications.setModifications(modList.toArray(new Order[0]));
    return modifications;
  }

  public List<ProductModel> getAllProducts(String name) {
    List<ProductModel> products = new ArrayList<>();
    if (name == null) {
      productRepository.findAll().forEach(products::add);
    } else {
      productRepository.findByName(name).forEach(products::add);
    }
    return products;
  }

  public void reduceProductQuantity(String productId, Integer quantity) {
    Optional<ProductModel> productQuery = getProductById(productId);
    if (productQuery.isPresent()) {
      ProductModel product = productQuery.get();
      product.setQuantity(product.getQuantity() - quantity);
      productRepository.save(product);
    }
  }

  public Optional<ProductModel> getProductById(String id) {
    return productRepository.findById(id);
  }

  public List<ProductModel> getAllProductsByUserId(String userId) {
    return productRepository.findAllByUserId(userId);
  }

  public ProductModel createProduct(ProductDTO productModel) {
    return productRepository.save(
        new ProductModel(
            productModel.getName(),
            productModel.getDescription(),
            productModel.getPrice(),
            productModel.getQuantity(),
            productModel.getUserId()));
  }

  public Optional<ProductModel> updateProduct(String id, ProductDTO productDTO) {
    ProductModel productModel =
        new ProductModel(
            productDTO.getName(),
            productDTO.getDescription(),
            productDTO.getPrice(),
            productDTO.getQuantity(),
            productDTO.getUserId());

    return updateProduct(id, productModel);
  }

  public Optional<ProductModel> updateProduct(String id, ProductModel productModel) {
    Optional<ProductModel> productData = productRepository.findById(id);

    if (productData.isPresent()) {
      ProductModel product = productData.get();
      product.setName(productModel.getName());
      product.setDescription(productModel.getDescription());
      product.setQuantity(productModel.getQuantity());
      product.setPrice(productModel.getPrice());
      return Optional.of(productRepository.save(product));
    } else {
      return Optional.empty();
    }
  }

  public void deleteProduct(String id) {
    kafkaService.deleteProductMedia(id);
    productRepository.deleteById(id);
  }

  public void deleteAllUserProducts(String userId) {
    List<ProductModel> products = productRepository.findAllByUserId(userId);
    if (!products.isEmpty()) {
      products.forEach(
          product -> {
            String id = product.getId();
            kafkaService.deleteProductMedia(product.getId());
            productRepository.deleteById(id);
          });
    }
  }

  public void handleProductOrderCancellation(String productId, Integer quantity) {
    Optional<ProductModel> productQuery = productRepository.findById(productId);
    if (productQuery.isPresent()) {
      ProductModel product = productQuery.get();
      product.setQuantity(product.getQuantity() + quantity);
      productRepository.save(product);
    }
  }
}
