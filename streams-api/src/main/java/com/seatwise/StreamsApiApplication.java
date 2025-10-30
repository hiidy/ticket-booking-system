package com.seatwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class StreamsApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(StreamsApiApplication.class, args);
  }
}
