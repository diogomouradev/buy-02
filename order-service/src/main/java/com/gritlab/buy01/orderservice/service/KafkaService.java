package com.gritlab.buy01.orderservice.service;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gritlab.buy01.orderservice.cache.CachedTokenInfo;
import com.gritlab.buy01.orderservice.event.UserProfileDeletionEvent;
import com.gritlab.buy01.orderservice.kafka.message.CartValidationRequest;
import com.gritlab.buy01.orderservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.orderservice.kafka.message.ProductOrderCancellationMessage;
import com.gritlab.buy01.orderservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.orderservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.orderservice.kafka.message.UserProfileDeleteMessage;

@Service
public class KafkaService {
  private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
  private static final String TOPIC_REQUEST = "token-validation-request";
  private static final String TOPIC_RESPONSE = "token-validation-response";
  private static final String CART_VALIDATION_REQUEST = "cart-validation-request";
  private static final String CART_VALIDATION_RESPONSE = "cart-validation-response";
  private static final String PRODUCT_ORDER_CANCELLATION_MESSAGE = "product-order-cancellation";
  private static final long TOKEN_CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);

  //Used for producing messages to Kafka topics.
  @Qualifier("kafkaTemplate")
  private final KafkaTemplate<String, TokenValidationRequest> kafkaTemplate;
 
  @Qualifier("cartValidationKafkaTemplate")
  private final KafkaTemplate<String, CartValidationRequest> cartValidationKafkaTemplate;

  @Qualifier("productOrderCancellationKafkaTemplate")
  private final KafkaTemplate<String, ProductOrderCancellationMessage>
      productOrderCancellationKafkaTemplate;

  // token validation responses are cached to limit the number of kafka messages
  private ConcurrentMap<String, CachedTokenInfo> tokenCache = new ConcurrentHashMap<>();

  private ApplicationEventPublisher eventPublisher;

  @Autowired
  public KafkaService(
      KafkaTemplate<String, TokenValidationRequest> kafkaTemplate,
      KafkaTemplate<String, CartValidationRequest> cartValidationKafkaTemplate,
      KafkaTemplate<String, ProductOrderCancellationMessage>
          productOrderCancellationKafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
    this.cartValidationKafkaTemplate = cartValidationKafkaTemplate;
    this.productOrderCancellationKafkaTemplate = productOrderCancellationKafkaTemplate;
  }

  private ConcurrentMap<String, BlockingQueue<TokenValidationResponse>> responseQueues =
      new ConcurrentHashMap<>();

  private ConcurrentMap<String, BlockingQueue<CartValidationResponse>>
      cartValidationResponseQueues = new ConcurrentHashMap<>();

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
  //Used for consuming messages from Kafka topics.
  @KafkaListener(topics = TOPIC_RESPONSE, groupId = "order-service-group")
  public void consumeTokenValidationResponse(TokenValidationResponse response) {
    BlockingQueue<TokenValidationResponse> queue = responseQueues.get(response.getCorrelationId());
    if (queue != null) {
      boolean queued = queue.offer(response);
      if (!queued) {
        logger.error(
            "Error: token validation response by correlationId {} could not be placed in queue",
            response.getCorrelationId());
      }
    }
  }

  public CartValidationResponse sendCartValidationRequestAndWaitForResponse(
      CartValidationRequest request) {
    BlockingQueue<CartValidationResponse> responseQueue = new ArrayBlockingQueue<>(1);

    String correlationId = request.getCorrelationId();

    cartValidationResponseQueues.put(correlationId, responseQueue);

    cartValidationKafkaTemplate.send(CART_VALIDATION_REQUEST, request);

    try {
      return responseQueue.poll(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      cartValidationResponseQueues.remove(correlationId);
    }
  }

  @KafkaListener(
      topics = CART_VALIDATION_RESPONSE,
      groupId = "cart-validation-group",
      containerFactory = "cartValidationResponseContainerFactory")
  public void consumeCartValidationResponse(CartValidationResponse response) {
    BlockingQueue<CartValidationResponse> queue =
        cartValidationResponseQueues.get(response.getCorrelationId());
    if (queue != null) {
      boolean queued = queue.offer(response);
      if (!queued) {
        logger.error(
            "Error: cart validation response by correlationId {} could not be placed in queue",
            response.getCorrelationId());
      }
    }
  }

  public void sendProductOrderCancellation(ProductOrderCancellationMessage message) {
    productOrderCancellationKafkaTemplate.send(PRODUCT_ORDER_CANCELLATION_MESSAGE, message);
  }

  @KafkaListener(
      topics = "user-products-deletion",
      groupId = "order-user-profile-deletion-group",
      containerFactory = "userProfileDeleteMessageContainerFactory")
  public void consumerUserProfileDeleteMessage(UserProfileDeleteMessage message) {
    eventPublisher.publishEvent(
        new UserProfileDeletionEvent(this, message.getCorrelationId(), message.getUserId()));
  }
}
