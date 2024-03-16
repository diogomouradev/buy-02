package com.gritlab.buy01.mediaservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.gritlab.buy01.mediaservice.model.Media;

public interface MediaRepository extends MongoRepository<Media, String> {
  Optional<Media> findById(String id);

  Optional<List<Media>> findAllByProductId(String productId);

  Optional<Media> findByUserId(String userId);

  Optional<List<Media>> findAllByUserId(String userId);

  void deleteById(String id);

  void deleteByUserId(String userId);

  void deleteByProductId(String productId);

  void deleteAllByProductId(String productId);

  Optional<Media> findFirstByProductId(String productId);

  Optional<Media> findFirstByUserId(String userId);

  void deleteAllByUserId(String userId);
}
