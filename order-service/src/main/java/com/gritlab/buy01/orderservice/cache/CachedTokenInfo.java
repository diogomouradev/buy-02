package com.gritlab.buy01.orderservice.cache;

import java.time.Instant;

import com.gritlab.buy01.orderservice.kafka.message.TokenValidationResponse;

public class CachedTokenInfo {
  private TokenValidationResponse validationResponse;
  private Instant cachedAt;

  public CachedTokenInfo(TokenValidationResponse validationResponse, Instant cachedAt) {
    this.validationResponse = validationResponse;
    this.cachedAt = cachedAt;
  }

  public TokenValidationResponse getValidationResponse() {
    return validationResponse;
  }

  public Instant getCachedAt() {
    return cachedAt;
  }
}
