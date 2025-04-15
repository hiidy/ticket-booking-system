package com.seatwise.queue;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "queue")
@Getter
public class QueueProperties {

  private final int totalShard;

  @ConstructorBinding
  public QueueProperties(int totalShard) {
    this.totalShard = totalShard;
  }
}
