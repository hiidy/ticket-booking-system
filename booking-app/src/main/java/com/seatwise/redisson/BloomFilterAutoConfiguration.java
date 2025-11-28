package com.seatwise.redisson;

import java.util.LinkedHashMap;
import java.util.Map;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@AutoConfiguration(after = RedissonAutoConfigurationV2.class)
@EnableConfigurationProperties(BloomFilterProperties.class)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(
    prefix = "bloom-filter",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class BloomFilterAutoConfiguration {

  @Bean(name = "bloomFilters")
  @ConditionalOnMissingBean(name = "bloomFilters")
  public Map<String, BloomFilterHandler> bloomFilters(
      RedissonClient redissonClient, BloomFilterProperties properties) {
    Map<String, BloomFilterProperties.FilterConfig> filterConfigs = properties.getFilters();
    if (filterConfigs == null || filterConfigs.isEmpty()) {
      throw new IllegalStateException("bloom-filter.filters는 활성화된 경우 비어있을 수 없습니다");
    }

    Map<String, BloomFilterHandler> filters = new LinkedHashMap<>(filterConfigs.size());
    filterConfigs.forEach(
        (filterName, config) -> {
          if (!StringUtils.hasText(filterName)) {
            throw new IllegalArgumentException("Bloom 필터 이름은 비어있을 수 없습니다");
          }
          if (config == null) {
            throw new IllegalArgumentException("Bloom 필터 설정은 null일 수 없습니다: " + filterName);
          }
          filters.put(
              filterName,
              new BloomFilterHandler(
                  redissonClient,
                  filterName,
                  config.getExpectedInsertions(),
                  config.getFalseProbability()));
        });
    return filters;
  }
}
