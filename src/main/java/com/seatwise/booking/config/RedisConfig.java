package com.seatwise.booking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.messaging.rebalancer.RebalanceMessage;
import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
    return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    template.setDefaultSerializer(serializer);

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  public StreamMessageListenerContainerOptions<String, ObjectRecord<String, BookingMessage>>
      streamOptions(ObjectMapper redisObjectMapper) {
    return StreamMessageListenerContainerOptions.builder()
        .pollTimeout(Duration.ofMillis(100))
        .keySerializer(new StringRedisSerializer())
        .hashKeySerializer(new StringRedisSerializer())
        .hashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper))
        .objectMapper(new Jackson2HashMapper(true))
        .targetType(BookingMessage.class)
        .build();
  }

  @Bean
  public StreamMessageListenerContainerOptions<String, ObjectRecord<String, RebalanceMessage>>
      rebalanceStreamOptions(ObjectMapper redisObjectMapper) {
    return StreamMessageListenerContainerOptions.builder()
        .pollTimeout(Duration.ofMillis(100))
        .keySerializer(new StringRedisSerializer())
        .hashKeySerializer(new StringRedisSerializer())
        .hashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper))
        .objectMapper(new Jackson2HashMapper(true))
        .targetType(RebalanceMessage.class)
        .build();
  }

  @Bean
  public StreamMessageListenerContainer<String, ObjectRecord<String, RebalanceMessage>>
      rebalanceStreamContainer(
          RedisConnectionFactory redisConnectionFactory,
          StreamMessageListenerContainerOptions<String, ObjectRecord<String, RebalanceMessage>>
              options) {
    return StreamMessageListenerContainer.create(redisConnectionFactory, options);
  }

  @Bean
  public StreamMessageListenerContainer<String, ObjectRecord<String, BookingMessage>>
      streamContainer(
          RedisConnectionFactory redisConnectionFactory,
          StreamMessageListenerContainerOptions<String, ObjectRecord<String, BookingMessage>>
              options) {
    return StreamMessageListenerContainer.create(redisConnectionFactory, options);
  }
}
