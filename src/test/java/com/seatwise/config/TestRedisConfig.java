package com.seatwise.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Profile("local")
@Configuration
public class TestRedisConfig {

  private static final Logger logger = LoggerFactory.getLogger(TestRedisConfig.class);
  private RedisServer redisServer;

  @Value("${spring.embedded.redis.port:6370}")
  private int redisPort;

  @PostConstruct
  public void startRedis() {
    try {
      redisServer = new RedisServer(redisPort);
      redisServer.start();
    } catch (Exception e) {
      logger.error("임베디드 Redis 서버 시작 실패: {}", e.getMessage(), e);
    }
  }

  @PreDestroy
  public void stopRedis() throws IOException {
    if (redisServer != null && redisServer.isActive()) {
      redisServer.stop();
    }
  }
}
