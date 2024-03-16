package com.gritlab.buy01.productservice.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

import com.gritlab.buy01.productservice.cache.CachedTokenInfo;
import com.gritlab.buy01.productservice.event.CartValidationEvent;
import com.gritlab.buy01.productservice.event.ProductOrderCancellationEvent;
import com.gritlab.buy01.productservice.event.ProductOwnershipValidationEvent;
import com.gritlab.buy01.productservice.event.UserProductsDeletionEvent;
import com.gritlab.buy01.productservice.kafka.message.CartValidationRequest;
import com.gritlab.buy01.productservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.productservice.kafka.message.ProductMediaDeleteMessage;
import com.gritlab.buy01.productservice.kafka.message.ProductOrderCancellationMessage;
import com.gritlab.buy01.productservice.kafka.message.ProductOwnershipRequest;
import com.gritlab.buy01.productservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.productservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.productservice.kafka.message.UserProfileDeleteMessage;
import com.gritlab.buy01.productservice.kafka.utils.ConcurrentHashSet;

@Service
public class KafkaService {
  private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

  private static final String TOPIC_REQUEST = "token-validation-request";
  private static final String TOPIC_RESPONSE = "token-validation-response";

  @Qualifier("tokenValidationRequestKafkaTemplate")
  private final KafkaTemplate<String, TokenValidationRequest> tokenValidationRequestKafkaTemplate;

  @Qualifier("productMediaDeleteMessageKafkaTemplate")
  private final KafkaTemplate<String, ProductMediaDeleteMessage>
      productMediaDeleteMessageKafkaTemplate;

  @Qualifier("cartValidationResponseKafkaTemplate")
  private final KafkaTemplate<String, CartValidationResponse> cartValidationResponseKafkaTemplate;

  private ApplicationEventPublisher eventPublisher;

  // token validation responses are cached to limit the number of kafka messages
  private ConcurrentMap<String, CachedTokenInfo> tokenCache = new ConcurrentHashMap<>();
  private static final long TOKEN_CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);

  @Autowired
  public KafkaService(
      KafkaTemplate<String, TokenValidationRequest> tokenValidationRequestKafkaTemplate,
      KafkaTemplate<String, ProductMediaDeleteMessage> productMediaDeleteMessageKafkaTemplate,
      KafkaTemplate<String, CartValidationResponse> cartValidationResponseKafkaTemplate,
      ApplicationEventPublisher eventPublisher) {
    this.tokenValidationRequestKafkaTemplate = tokenValidationRequestKafkaTemplate;
    this.productMediaDeleteMessageKafkaTemplate = productMediaDeleteMessageKafkaTemplate;
    this.cartValidationResponseKafkaTemplate = cartValidationResponseKafkaTemplate;
    this.eventPublisher = eventPublisher;
  }

  public ConcurrentMap<String, CachedTokenInfo> getTokenCache() {
    return tokenCache;
  }

  // to track processed messages and ensure idempotency
  private Set<String> processedCorrelationIds = new HashSet<>();

  public Set<String> getProcessedCorrelationIds() {
    return processedCorrelationIds;
  }

  private ConcurrentHashSet<String> processedOrderCorrelationIds = new ConcurrentHashSet<>();

  private ConcurrentMap<String, BlockingQueue<TokenValidationResponse>> responseQueues =
      new ConcurrentHashMap<>();

  public ConcurrentMap<String, BlockingQueue<TokenValidationResponse>> getResponseQueues() {
    return responseQueues;
  }

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
    tokenValidationRequestKafkaTemplate.send(TOPIC_REQUEST, request);

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

  @KafkaListener(
      topics = TOPIC_RESPONSE,
      groupId = "product-service-group",
      containerFactory = "kafkaListenerContainerFactory")
  public void consumeTokenValidationResponse(TokenValidationResponse response) {
    BlockingQueue<TokenValidationResponse> queue = responseQueues.get(response.getCorrelationId());
    if (queue != null) {
      boolean queued = queue.offer(response);
      if (!queued) {
        logger.error(
            "Error: token validation response with correlationId {} could not be placed in queue",
            response.getCorrelationId());
      }
    }
  }

  @KafkaListener(
      topics = "user-products-deletion",
      groupId = "user-product-deletion-group",
      containerFactory = "kafkaProductDeletionContainerFactory")
  public void deleteUserProducts(UserProfileDeleteMessage request) {
    eventPublisher.publishEvent(new UserProductsDeletionEvent(this, request.getUserId()));
  }

  public void deleteProductMedia(String productId) {
    String correlationId = UUID.randomUUID().toString();

    if (processedCorrelationIds.contains(correlationId)) {
      logger.warn("Duplicate deletion request detected for product: {}", productId);
      return;
    }

    ProductMediaDeleteMessage message = new ProductMediaDeleteMessage(correlationId, productId);

    productMediaDeleteMessageKafkaTemplate.send("product-media-deletion", message);

    processedCorrelationIds.add(correlationId);
  }

  @KafkaListener(
      topics = "product-ownership-requests",
      groupId = "product-ownership-request-group",
      containerFactory = "kafkaProductOwnershipRequestListenerContainerFactory")
  public void validateOwnership(ProductOwnershipRequest request) {
    eventPublisher.publishEvent(
        new ProductOwnershipValidationEvent(
            this, request.getProductId(), request.getUserId(), request.getCorrelationId()));
  }

  @KafkaListener(
      topics = "cart-validation-request",
      groupId = "cart-validation-request-group",
      containerFactory = "kafkaCartValidationRequestListenerContainerFactory")
  public void validateCart(CartValidationRequest request) {
    String correlationId = request.getCorrelationId();

    if (!processedOrderCorrelationIds.contains(correlationId)) {
      eventPublisher.publishEvent(
          new CartValidationEvent(this, request.getCorrelationId(), request.getCart()));

      processedOrderCorrelationIds.add(correlationId);
    }
  }

  public void sendCartValidationResponse(CartValidationResponse response) {
    cartValidationResponseKafkaTemplate.send("cart-validation-response", response);
  }

  @KafkaListener(
      topics = "product-order-cancellation",
      groupId = "product-order-cancellation-group",
      containerFactory = "kafkaProductOrderCancellationMessageListenerContainerFactory")
  public void cancelProductOrder(ProductOrderCancellationMessage message) {
    String correlationId = message.getCorrelationId();

    if (!processedCorrelationIds.contains(correlationId)) {
      eventPublisher.publishEvent(
          new ProductOrderCancellationEvent(
              this, message.getCorrelationId(), message.getProductId(), message.getQuantity()));

      processedCorrelationIds.add(correlationId);
    }
  }
}
