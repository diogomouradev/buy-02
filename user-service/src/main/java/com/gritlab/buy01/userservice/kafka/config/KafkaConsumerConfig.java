package com.gritlab.buy01.userservice.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.gritlab.buy01.userservice.kafka.message.TokenValidationRequest;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarUpdateRequest;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-service-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    // props.put(JsonDeserializer.TRUSTED_PACKAGES,
    // "com.gritlab.buy01.productservice.kafka.message");
    // props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TokenValidationRequest.class);
    // props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
    return props;
  }

  @Bean
  public ConsumerFactory<String, TokenValidationRequest> consumerFactory() {
    JsonDeserializer<TokenValidationRequest> deserializer =
        new JsonDeserializer<>(TokenValidationRequest.class);
    deserializer.setUseTypeHeaders(false);
    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TokenValidationRequest>
      kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, TokenValidationRequest> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  @Bean
  public ConsumerFactory<String, UserAvatarUpdateRequest> userAvatarUpdateRequestConsumerFactory() {
    Map<String, Object> configs = consumerConfigs();
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, "user-avatar-update-requests-group");
    JsonDeserializer<UserAvatarUpdateRequest> deserializer =
        new JsonDeserializer<>(UserAvatarUpdateRequest.class);
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, UserAvatarUpdateRequest>
      kafkaUserAvatarUpdateRequestContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, UserAvatarUpdateRequest> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(userAvatarUpdateRequestConsumerFactory());
    factory.setConcurrency(3);
    return factory;
  }
}
