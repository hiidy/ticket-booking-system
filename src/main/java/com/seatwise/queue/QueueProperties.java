package com.seatwise.queue;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "queue")
@Getter
public class QueueProperties {

  private final int shardCount;
  private final int instanceCount;
  private final String consumerGroup;

  @ConstructorBinding
  public QueueProperties(int shardCount, int instanceCount, String consumerGroup) {
    this.shardCount = shardCount;
    this.instanceCount = instanceCount;
    this.consumerGroup = consumerGroup;
  }
}
