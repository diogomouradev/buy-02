package com.gritlab.buy01.orderservice.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.gritlab.buy01.orderservice.model.Order;

public interface OrderRepository extends MongoRepository<Order, String> {
  Optional<Order> findById(String id);

  Optional<Order[]> findAllBySellerId(String sellerId);

  Optional<Order[]> findAllByBuyerId(String buyerId);
}
