package com.seatwise.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import redis.embedded.RedisServer;

@TestConfiguration
public class RedisTestConfig {

  private RedisServer redisServer;
  private int redisPort;

  public RedisTestConfig() {
    redisPort = findAvailablePort();
  }

  private int findAvailablePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      return 16379;
    }
  }

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
  @Primary
  public RedissonClient redissonTestClient() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://localhost:" + redisPort);

    return Redisson.create(config);
  }
}
