package com.seatwise.booking.messaging;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "queue")
@Getter
public class MessagingProperties {

  @Min(value = 1, message = "샤드 수는 최소 1 이상이어야 합니다.")
  private final int partitionCount;

  @Min(value = 1, message = "인스턴스 수는 최소 1 이상이어야 합니다.")
  private final int instanceCount;

  @NotBlank(message = "소비자 그룹 이름은 필수입니다.")
  private final String consumerGroup;

  @ConstructorBinding
  public MessagingProperties(
      @DefaultValue("32") int partitionCount,
      @DefaultValue("1") int instanceCount,
      @DefaultValue("booking-group") String consumerGroup) {
    this.partitionCount = partitionCount;
    this.instanceCount = instanceCount;
    this.consumerGroup = consumerGroup;
  }
}
