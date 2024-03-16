package com.gritlab.buy01.mediaservice.service;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.mediaservice.cache.CachedTokenInfo;
import com.gritlab.buy01.mediaservice.kafka.message.ProductMediaDeleteMessage;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipRequest;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarDeleteMessage;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateRequest;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateResponse;

@Service
public class KafkaService {
  private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
  private static final String TOPIC_REQUEST = "token-validation-request";
  private static final String TOPIC_RESPONSE = "token-validation-response";

  // token validation responses are cached to limit the number of kafka messages
  private ConcurrentMap<String, CachedTokenInfo> tokenCache = new ConcurrentHashMap<>();
  private static final long TOKEN_CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);

  @Autowired
  @Qualifier("kafkaTemplate")
  private KafkaTemplate<String, TokenValidationRequest> kafkaTemplate;

  @Autowired
  @Qualifier("productOwnershipRequestKafkaTemplate")
  private KafkaTemplate<String, ProductOwnershipRequest> productOwnerShipKafkaTemplate;

  @Autowired
  @Qualifier("userAvatarUpdateRequestKafkaTemplate")
  private KafkaTemplate<String, UserAvatarUpdateRequest> userAvatarUpdateRequestKafkaTemplate;

  @Autowired private MediaService mediaService;

  private ConcurrentMap<String, BlockingQueue<TokenValidationResponse>> responseQueues =
      new ConcurrentHashMap<>();

  private ConcurrentMap<String, BlockingQueue<ProductOwnershipResponse>>
      productOwnershipResponseQueues = new ConcurrentHashMap<>();

  private ConcurrentMap<String, BlockingQueue<UserAvatarUpdateResponse>>
      userAvatarUpdateResponseQueues = new ConcurrentHashMap<>();

  public TokenValidationResponse validateTokenWithUserMicroservice(TokenValidationRequest request) {
    // checking the cache first
    CachedTokenInfo cachedTokenInfo = tokenCache.get(request.getJwtToken());
    if (cachedTokenInfo != null) {
      // Check if cached token info is still valid
      if ((Instant.now().toEpochMilli() - cachedTokenInfo.getCachedAt().toEpochMilli())
          < TOKEN_CACHE_DURATION) {
        return cachedTokenInfo.getValidationResponse(); // return cached response
      } else {
        tokenCache.remove(request.getJwtToken()); // remove expired token info from cache
      }
    }

    BlockingQueue<TokenValidationResponse> queue = new ArrayBlockingQueue<>(1);
    if (request.getCorrelationId() != null) {
      responseQueues.put(request.getCorrelationId(), queue);
    } else {
      return null;
    }
    kafkaTemplate.send(TOPIC_REQUEST, request);

    // wait for a response for a maximum of 5 seconds
    try {
      TokenValidationResponse response = queue.poll(5, TimeUnit.SECONDS);
      responseQueues.remove(request.getCorrelationId());
      if (response != null && response.getErrorMessage() == null) {
        tokenCache.put(response.getJwtToken(), new CachedTokenInfo(response, Instant.now()));
      }
      return response;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      responseQueues.remove(request.getCorrelationId());
      logger.error("Thread was interrupted", e);
      return null;
    }
  }

  @KafkaListener(topics = TOPIC_RESPONSE, groupId = "media-service-group")
  public void consumeTokenValidationResponse(TokenValidationResponse response) {
    BlockingQueue<TokenValidationResponse> queue = responseQueues.get(response.getCorrelationId());
    if (queue != null) {
      queue.offer(response);
    }
  }

  @KafkaListener(
      topics = "user-avatar-deletion",
      groupId = "user-avatar-deletion-group",
      containerFactory = "kafkaAvatarDeletionContainerFactory")
  public void deleteUserAvatar(UserAvatarDeleteMessage request) {
    mediaService.requestDeleteAllUserAvatars(request.getUserId());
  }

  @KafkaListener(
      topics = "product-media-deletion",
      groupId = "product-media-deletion-group",
      containerFactory = "kafkaProductMediaDeletionContainerFactory")
  public void deleteProductMedia(ProductMediaDeleteMessage request) {
    mediaService.requestDeleteAllProductMedia(request.getProductId());
  }

  public void sendProductOwnershipRequest(String productId, String userId) {
    ProductOwnershipRequest request = new ProductOwnershipRequest(productId, userId);
    productOwnerShipKafkaTemplate.send("product-ownership-requests", request);
  }

  @KafkaListener(
      topics = "product-ownership-responses",
      groupId = "media-service-group",
      containerFactory = "kafkaProductOwnershipResponseListenerContainerFactory")
  public void handleProductOwnershipResponse(ProductOwnershipResponse response) {
    // Retrieve the queue using correlation ID from the response
    BlockingQueue<ProductOwnershipResponse> queue =
        productOwnershipResponseQueues.get(response.getCorrelationId());

    if (queue != null) {
      queue.offer(response); // Place the response into the queue
    }
  }

  public ProductOwnershipResponse sendProductOwnershipRequestAndWaitForResponse(
      ProductOwnershipRequest request) {
    BlockingQueue<ProductOwnershipResponse> responseQueue = new ArrayBlockingQueue<>(1);

    String correlationId = request.getCorrelationId();

    productOwnershipResponseQueues.put(correlationId, responseQueue);

    productOwnerShipKafkaTemplate.send("product-ownership-requests", request);

    try {
      return responseQueue.poll(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      productOwnershipResponseQueues.remove(correlationId);
    }
  }

  public UserAvatarUpdateResponse sendUserAvatarUpdateRequestAndWaitForResponse(
      UserAvatarUpdateRequest request) {
    BlockingQueue<UserAvatarUpdateResponse> responseQueue = new ArrayBlockingQueue<>(1);

    String correlationId = request.getCorrelationId();

    userAvatarUpdateResponseQueues.put(correlationId, responseQueue);

    userAvatarUpdateRequestKafkaTemplate.send("user-avatar-update-requests", request);

    try {
      return responseQueue.poll(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      userAvatarUpdateResponseQueues.remove(correlationId);
    }
  }

  @KafkaListener(
      topics = "user-avatar-update-responses",
      groupId = "media-service-group",
      containerFactory = "kafkaUserAvatarUpdateResponseListenerContainerFactory")
  public void handleUserAvatarUpdateResponse(UserAvatarUpdateResponse response) {
    BlockingQueue<UserAvatarUpdateResponse> queue =
        userAvatarUpdateResponseQueues.get(response.getCorrelationId());

    if (queue != null) {
      queue.offer(response); // Place the response into the queue
    }
  }
}
