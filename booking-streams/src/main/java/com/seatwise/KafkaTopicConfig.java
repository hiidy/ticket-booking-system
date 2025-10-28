package com.seatwise;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
@RequiredArgsConstructor
public class KafkaTopicConfig {

  private final KafkaTopicProperties topicProperties;

  @Bean
  public NewTopic bookingRequestTopic() {
    return TopicBuilder.name(topicProperties.bookingRequest())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .compact()
        .build();
  }

  @Bean
  public NewTopic ticketTopic() {
    return TopicBuilder.name(topicProperties.ticketState())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .compact()
        .build();
  }

  @Bean
  public NewTopic bookingCommandTopic() {
    return TopicBuilder.name(topicProperties.bookingCommand())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .compact()
        .build();
  }

  @Bean
  public NewTopic bookingCompletedTopic() {
    return TopicBuilder.name(topicProperties.bookingCompleted())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .compact()
        .build();
  }

  @Bean
  public NewTopic bookingResultTopic() {
    return TopicBuilder.name(topicProperties.bookingResult())
        .partitions(topicProperties.partitions())
        .replicas(topicProperties.replicas())
        .compact()
        .build();
  }
}
