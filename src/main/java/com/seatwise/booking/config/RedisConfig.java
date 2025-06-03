package com.seatwise.booking.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seatwise.booking.dto.BookingMessage;
import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
    return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(
        objectMapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        As.PROPERTY);
    return objectMapper;
  }

  private <T> RedisTemplate<String, T> createRedisTemplate(
      RedisConnectionFactory factory, Class<T> targetClass) {

    RedisTemplate<String, T> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    ObjectMapper objectMapper = createObjectMapper();

    Jackson2JsonRedisSerializer<T> serializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, targetClass);

    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    return createRedisTemplate(factory, Object.class);
  }

  @Bean
  public StreamMessageListenerContainerOptions<String, ObjectRecord<String, BookingMessage>>
      streamOptions(RedisTemplate<String, Object> redisTemplate) {
    return StreamMessageListenerContainerOptions.builder()
        .pollTimeout(Duration.ofMillis(100))
        .hashValueSerializer(redisTemplate.getValueSerializer())
        .objectMapper(new Jackson2HashMapper(true))
        .targetType(BookingMessage.class)
        .build();
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
