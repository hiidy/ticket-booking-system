package com.seatwise;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.streams.KafkaStreamsInteractiveQueryService;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class KafkaStreamsQueryConfig {

  @Bean
  public KafkaStreamsInteractiveQueryService kafkaStreamsInteractiveQueryService(
      StreamsBuilderFactoryBean streamsBuilderFactoryBean) {

    KafkaStreamsInteractiveQueryService queryService =
        new KafkaStreamsInteractiveQueryService(streamsBuilderFactoryBean);

    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
    RetryPolicy retryPolicy = new SimpleRetryPolicy(5);
    retryTemplate.setRetryPolicy(retryPolicy);
    queryService.setRetryTemplate(retryTemplate);

    return queryService;
  }
}
