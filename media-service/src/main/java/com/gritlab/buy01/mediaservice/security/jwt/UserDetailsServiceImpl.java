package com.gritlab.buy01.mediaservice.security.jwt;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.mediaservice.model.User;
import com.gritlab.buy01.mediaservice.security.UserDetailsImpl;
import com.gritlab.buy01.mediaservice.service.KafkaService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired private KafkaService kafkaService;

  @Override
  public UserDetails loadUserByUsername(String token) throws UsernameNotFoundException {
    TokenValidationRequest request =
        new TokenValidationRequest(token, UUID.randomUUID().toString());
    TokenValidationResponse response = kafkaService.validateTokenWithUserMicroservice(request);

    if (response == null || response.getUserId() == null) {
      throw new UsernameNotFoundException("User Not Found for token: " + token);
    }
    User user = new User(response.getUserId(), response.getName(), response.getRole());
    return UserDetailsImpl.build(user);
  }
}
