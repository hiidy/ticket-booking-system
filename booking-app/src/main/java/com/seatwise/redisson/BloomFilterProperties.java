package com.seatwise.redisson;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "bloom-filter")
public class BloomFilterProperties {

  private boolean enabled = true;

  private Map<String, FilterConfig> filters;

  @Data
  public static class FilterConfig {

    private long expectedInsertions = 10000L;

    private double falseProbability = 0.01D;
  }
}
