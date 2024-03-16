package com.gritlab.buy01.userservice.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.userservice.event.UserDeletionEvent;
import com.gritlab.buy01.userservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.userservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarDeleteMessage;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarUpdateRequest;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarUpdateResponse;
import com.gritlab.buy01.userservice.kafka.message.UserProfileDeleteMessage;
import com.gritlab.buy01.userservice.model.User;

@Service
public class KafkaService {

  private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

  private final AuthService authService;
  private final UserService userService;

  @EventListener
  public void handleUserDeletionEvent(UserDeletionEvent event) {
    User user = event.getUser();
    deleteUserItems(user);
  }

  @Qualifier("tokenValidationResponseKafkaTemplate")
  private final KafkaTemplate<String, TokenValidationResponse> tokenValidationResponseKafkaTemplate;

  @Qualifier("userProfileDeleteMessageKafkaTemplate")
  private final KafkaTemplate<String, UserProfileDeleteMessage>
      userProfileDeleteMessageKafkaTemplate;

  @Qualifier("userAvatarDeleteMessageKafkaTemplate")
  private KafkaTemplate<String, UserAvatarDeleteMessage> userAvatarDeleteMessageKafkaTemplate;

  @Qualifier("userAvatarUpdateResponseKafkaTemplate")
  private final KafkaTemplate<String, UserAvatarUpdateResponse>
      userAvatarUpdateResponseKafkaTemplate;

  @Value("${kafka.topic.token-validation-response}")
  private String responseTopic;

  // To track processed messages and ensure idempotency
  private Set<String> processedCorrelationIds = new HashSet<>();

  @Autowired
  public KafkaService(
      AuthService authService,
      UserService userService,
      KafkaTemplate<String, TokenValidationResponse> tokenValidationResponseKafkaTemplate,
      KafkaTemplate<String, UserProfileDeleteMessage> userProfileDeleteMessageKafkaTemplate,
      KafkaTemplate<String, UserAvatarDeleteMessage> userAvatarDeleteMessageKafkaTemplate,
      KafkaTemplate<String, UserAvatarUpdateResponse> userAvatarUpdateResponseKafkaTemplate) {
    this.authService = authService;
    this.userService = userService;
    this.tokenValidationResponseKafkaTemplate = tokenValidationResponseKafkaTemplate;
    this.userProfileDeleteMessageKafkaTemplate = userProfileDeleteMessageKafkaTemplate;
    this.userAvatarDeleteMessageKafkaTemplate = userAvatarDeleteMessageKafkaTemplate;
    this.userAvatarUpdateResponseKafkaTemplate = userAvatarUpdateResponseKafkaTemplate;
  }

  public void sendMessage(TokenValidationResponse message) {
    tokenValidationResponseKafkaTemplate.send(responseTopic, message);
  }

  @KafkaListener(topics = "token-validation-request", groupId = "user-service-group")
  public void consumeMessage(TokenValidationRequest message) {
    try {
      // Check if this message was already processed
      if (processedCorrelationIds.contains(message.getCorrelationId())) {
        logger.warn(
            "Duplicate message detected with correlationId: {}", message.getCorrelationId());
        return;
      }

      User userDetails = authService.getUserDetailsFromToken(message.getJwtToken());
      TokenValidationResponse response = new TokenValidationResponse();
      response.setCorrelationId(message.getCorrelationId());
      response.setJwtToken(message.getJwtToken());

      if (userDetails == null) {
        response.setErrorMessage("Error: invalid token");
      } else {
        response.setName(userDetails.getName());
        response.setEmail(userDetails.getEmail());
        response.setRole(userDetails.getRole().toString());
        response.setUserId(userDetails.getId());
      }

      // Send the response
      sendMessage(response);

      // Mark this message as processed
      processedCorrelationIds.add(message.getCorrelationId());

    } catch (Exception e) {
      logger.error(
          "Error processing message with correlationId: {}", message.getCorrelationId(), e);
    }
  }

  public void deleteUserItems(User user) {
    String correlationId = UUID.randomUUID().toString();

    if (processedCorrelationIds.contains(correlationId)) {
      logger.warn("Duplicate deletion request detected for user: {}", user.getId());
      return;
    }

    UserProfileDeleteMessage message = new UserProfileDeleteMessage(correlationId, user.getId());
    if (user.getAvatar() != null) {
      String avatarCorrelationId = UUID.randomUUID().toString();
      UserAvatarDeleteMessage avatarDeleteMessage =
          new UserAvatarDeleteMessage(avatarCorrelationId, user.getId());
      userAvatarDeleteMessageKafkaTemplate.send("user-avatar-deletion", avatarDeleteMessage);
      processedCorrelationIds.add(avatarCorrelationId);
    }

    userProfileDeleteMessageKafkaTemplate.send("user-products-deletion", message);

    processedCorrelationIds.add(correlationId);
  }

  @KafkaListener(
      topics = "user-avatar-update-requests",
      groupId = "user-avatar-update-requests-group",
      containerFactory = "kafkaUserAvatarUpdateRequestContainerFactory")
  public void consumeUserAvatarUpdateRequests(UserAvatarUpdateRequest request) {
    try {
      if (processedCorrelationIds.contains(request.getCorrelationId())) {
        logger.warn(
            "Duplicate message detected with correlationId: {}", request.getCorrelationId());
        return;
      }

      Optional<User> user = userService.getUserById(request.getUserId());
      UserAvatarUpdateResponse response =
          new UserAvatarUpdateResponse(
              request.getCorrelationId(), request.getUserId(), request.getAvatarId(), false, "");

      if (user.isEmpty()) {
        response.setAllowed(false);
        response.setMessage("User not found");
      } else {
        boolean avatarUpdate = userService.updateAvatar(request.getUserId(), request.getAvatarId());
        response.setAllowed(avatarUpdate);
        response.setMessage(avatarUpdate ? "" : "Avatar update failed");
      }

      // send the response
      userAvatarUpdateResponseKafkaTemplate.send("user-avatar-update-responses", response);

    } catch (Exception e) {
      logger.error(
          "Error processing message with correlationId: {}", request.getCorrelationId(), e);
    }
  }
}
