package com.seatwise.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seatwise.queue.ProduceRequest;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

@Configuration
public class RedisConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    objectMapper.activateDefaultTyping(
        objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

    template.setDefaultSerializer(jackson2JsonRedisSerializer);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jackson2JsonRedisSerializer);
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jackson2JsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public StreamOperations<String, String, Object> streamOperations(
      RedisTemplate<String, Object> redisTemplate) {
    return redisTemplate.opsForStream(new Jackson2HashMapper(true));
  }

  @Bean
  public StreamMessageListenerContainerOptions<String, ObjectRecord<String, ProduceRequest>>
      streamOptions() {
    return StreamMessageListenerContainerOptions.builder()
        .pollTimeout(Duration.ofMillis(100))
        .hashValueSerializer(RedisSerializer.json())
        .objectMapper(new Jackson2HashMapper(true))
        .targetType(ProduceRequest.class)
        .build();
  }

  @Bean
  public StreamMessageListenerContainer<String, ObjectRecord<String, ProduceRequest>>
      streamContainer(
          RedisConnectionFactory redisConnectionFactory,
          StreamMessageListenerContainerOptions<String, ObjectRecord<String, ProduceRequest>>
              options) {
    return StreamMessageListenerContainer.create(redisConnectionFactory, options);
  }
}
