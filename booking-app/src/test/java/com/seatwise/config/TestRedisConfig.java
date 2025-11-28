package com.seatwise.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Profile("test")
@Configuration
public class TestRedisConfig {

  private static final Logger logger = LoggerFactory.getLogger(TestRedisConfig.class);
  private RedisServer redisServer;

  private int redisPort;

  @PostConstruct
  public void startRedis() {
    try {
      redisPort = findAvailablePort();
      redisServer = new RedisServer(redisPort);
      System.setProperty("spring.data.redis.port", String.valueOf(redisPort));
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

  private int findAvailablePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
