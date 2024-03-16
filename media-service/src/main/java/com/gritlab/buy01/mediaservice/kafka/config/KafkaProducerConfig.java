package com.gritlab.buy01.mediaservice.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipRequest;
import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateRequest;

@Configuration
public class KafkaProducerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<String, TokenValidationRequest> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, TokenValidationRequest> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  // ProductRequest before media upload
  @Bean
  public ProducerFactory<String, ProductOwnershipRequest>
      producerFactoryForProductOwnershipRequest() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, ProductOwnershipRequest> productOwnershipRequestKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForProductOwnershipRequest());
  }

  // user avatar update message
  @Bean
  public ProducerFactory<String, UserAvatarUpdateRequest>
      producerFactoryForUserAvatarUpdateRequest() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  public KafkaTemplate<String, UserAvatarUpdateRequest> userAvatarUpdateRequestKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForUserAvatarUpdateRequest());
  }
}
