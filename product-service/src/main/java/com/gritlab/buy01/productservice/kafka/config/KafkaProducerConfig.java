package com.gritlab.buy01.productservice.kafka.config;

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

import com.gritlab.buy01.productservice.kafka.message.CartValidationResponse;
import com.gritlab.buy01.productservice.kafka.message.ProductMediaDeleteMessage;
import com.gritlab.buy01.productservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.productservice.kafka.message.TokenValidationRequest;

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

  // User authentication request

  @Bean
  public ProducerFactory<String, TokenValidationRequest> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "tokenValidationRequestKafkaTemplate")
  public KafkaTemplate<String, TokenValidationRequest> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  // Product media deletion request

  @Bean
  public ProducerFactory<String, ProductMediaDeleteMessage> producerFactoryForProductMediaDelete() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "productMediaDeleteMessageKafkaTemplate")
  public KafkaTemplate<String, ProductMediaDeleteMessage> productMediaDeleteMessageKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForProductMediaDelete());
  }

  @Bean
  public ProducerFactory<String, ProductOwnershipResponse>
      producerFactoryForProductOwnershipResponse() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "productOwnershipResponseKafkaTemplate")
  public KafkaTemplate<String, ProductOwnershipResponse> productOwnershipResponseKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForProductOwnershipResponse());
  }

  @Bean
  public ProducerFactory<String, CartValidationResponse>
      producerFactoryForCartValidationResponse() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "cartValidationResponseKafkaTemplate")
  public KafkaTemplate<String, CartValidationResponse> cartValidationResponseKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForCartValidationResponse());
  }
}
