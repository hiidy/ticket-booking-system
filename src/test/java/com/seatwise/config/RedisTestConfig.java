package com.seatwise.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

@TestConfiguration
@Profile("test")
public class RedisTestConfig {

  private RedisServer redisServer;

  private int redisPort = 16379;

  @PostConstruct
  public void redisServer() throws IOException {
    redisServer = new RedisServer(redisPort);
    redisServer.start();
  }

  @PreDestroy
  public void stopRedis() throws IOException {
    if (redisServer != null) {
      redisServer.stop();
    }
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
    configuration.setHostName("localhost");
    configuration.setPort(redisPort);
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate() {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(redisConnectionFactory());
    return template;
  }
}
