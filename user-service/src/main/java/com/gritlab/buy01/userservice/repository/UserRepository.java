package com.gritlab.buy01.userservice.repository;

import com.gritlab.buy01.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByName(String name);

    Boolean existsByName(String name);

    Boolean existsByEmail(String email);
}
