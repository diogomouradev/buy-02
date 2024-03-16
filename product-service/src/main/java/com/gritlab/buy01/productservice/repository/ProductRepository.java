package com.gritlab.buy01.productservice.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.gritlab.buy01.productservice.model.ProductModel;

public interface ProductRepository extends MongoRepository<ProductModel, String> {
  List<ProductModel> findByName(String name);

  void deleteAllByUserId(String userId);

  List<ProductModel> findAllByUserId(String userId);
}
