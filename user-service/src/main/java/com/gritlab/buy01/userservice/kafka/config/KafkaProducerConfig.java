package com.gritlab.buy01.userservice.kafka.config;

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

import com.gritlab.buy01.userservice.kafka.message.TokenValidationResponse;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarDeleteMessage;
import com.gritlab.buy01.userservice.kafka.message.UserAvatarUpdateResponse;
import com.gritlab.buy01.userservice.kafka.message.UserProfileDeleteMessage;

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
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
    return props;
  }

  // User authentication by user and media service

  @Bean
  public ProducerFactory<String, TokenValidationResponse> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "tokenValidationResponseKafkaTemplate")
  public KafkaTemplate<String, TokenValidationResponse> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  // User products' deletion by product service

  @Bean
  public ProducerFactory<String, UserProfileDeleteMessage> producerFactoryForUserProfileDelete() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "userProfileDeleteMessageKafkaTemplate")
  public KafkaTemplate<String, UserProfileDeleteMessage> userProfileDeleteMessageKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForUserProfileDelete());
  }

  // User avatar deletion by media service

  @Bean
  public ProducerFactory<String, UserAvatarDeleteMessage> producerFactoryForUserAvatarDelete() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "userAvatarDeleteMessageKafkaTemplate")
  public KafkaTemplate<String, UserAvatarDeleteMessage> userAvatarDeleteMessageKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForUserAvatarDelete());
  }

  @Bean
  public ProducerFactory<String, UserAvatarUpdateResponse> producerFactoryForUserAvatarUpdate() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean(name = "userAvatarUpdateResponseKafkaTemplate")
  public KafkaTemplate<String, UserAvatarUpdateResponse> userAvatarUpdateResponseKafkaTemplate() {
    return new KafkaTemplate<>(producerFactoryForUserAvatarUpdate());
  }
}
