package com.seatwise.booking.messaging;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
@Getter
@Setter
public class MessagingProperties {

  @NotBlank(message = "소비자 그룹 이름은 필수입니다.")
  private String consumerGroup;

  @Min(value = 1, message = "샤드 수는 최소 1 이상이어야 합니다.")
  private int shardCount;

  @Min(value = 1, message = "인스턴스 수는 최소 1 이상이어야 합니다.")
  private int instanceCount;
}
