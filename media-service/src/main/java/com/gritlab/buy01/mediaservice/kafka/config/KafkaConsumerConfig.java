package com.gritlab.buy01.mediaservice.kafka.config;

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

import com.gritlab.buy01.mediaservice.kafka.message.ProductMediaDeleteMessage;
import com.gritlab.buy01.mediaservice.kafka.message.ProductOwnershipResponse;
import com.gritlab.buy01.mediaservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarDeleteMessage;
import com.gritlab.buy01.mediaservice.kafka.message.UserAvatarUpdateResponse;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    return props;
  }

  @Bean
  public ConsumerFactory<String, TokenValidationResponse> consumerFactory() {
    JsonDeserializer<TokenValidationResponse> deserializer =
        new JsonDeserializer<>(TokenValidationResponse.class);
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, TokenValidationResponse>
      kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, TokenValidationResponse> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setConcurrency(3); // set the amount of concurrent threads
    return factory;
  }

  @Bean
  public ConsumerFactory<String, UserAvatarDeleteMessage> userAvatarDeletionConsumerFactory() {
    Map<String, Object> configs = consumerConfigs();
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, "user-avatar-deletion-group");
    JsonDeserializer<UserAvatarDeleteMessage> deserializer =
        new JsonDeserializer<>(UserAvatarDeleteMessage.class);
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, UserAvatarDeleteMessage>
      kafkaAvatarDeletionContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, UserAvatarDeleteMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(userAvatarDeletionConsumerFactory());
    factory.setConcurrency(3);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, ProductMediaDeleteMessage> productMediaDeletionConsumerFactory() {
    Map<String, Object> configs = consumerConfigs();
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, "profile-media-deletion-group");
    JsonDeserializer<ProductMediaDeleteMessage> deserializer =
        new JsonDeserializer<>(ProductMediaDeleteMessage.class);
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ProductMediaDeleteMessage>
      kafkaProductMediaDeletionContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ProductMediaDeleteMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(productMediaDeletionConsumerFactory());
    factory.setConcurrency(3);
    return factory;
  }

  @Bean
  public ConsumerFactory<String, ProductOwnershipResponse>
      productOwnershipResponseConsumerFactory() {
    JsonDeserializer<ProductOwnershipResponse> deserializer =
        new JsonDeserializer<>(ProductOwnershipResponse.class);
    deserializer.setUseTypeHeaders(false); // adjust as per your requirements

    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ProductOwnershipResponse>
      kafkaProductOwnershipResponseListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, ProductOwnershipResponse> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(productOwnershipResponseConsumerFactory());
    factory.setConcurrency(3); // adjust as per your requirements
    return factory;
  }

  @Bean
  public ConsumerFactory<String, UserAvatarUpdateResponse>
      userAvatarUpdateResponseConsumerFactory() {
    JsonDeserializer<UserAvatarUpdateResponse> deserializer =
        new JsonDeserializer<>(UserAvatarUpdateResponse.class);
    deserializer.setUseTypeHeaders(false);

    return new DefaultKafkaConsumerFactory<>(
        consumerConfigs(), new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, UserAvatarUpdateResponse>
      kafkaUserAvatarUpdateResponseListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, UserAvatarUpdateResponse> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(userAvatarUpdateResponseConsumerFactory());
    factory.setConcurrency(3);
    return factory;
  }
}
