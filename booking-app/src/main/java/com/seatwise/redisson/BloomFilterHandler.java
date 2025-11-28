package com.seatwise.redisson;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

@Slf4j
public class BloomFilterHandler {

  private final RBloomFilter<String> bloomFilter;

  public BloomFilterHandler(
      RedissonClient redissonClient,
      String name,
      Long expectedInsertions,
      Double falseProbability) {
    String key = "seatwise:bloom:" + name;
    this.bloomFilter = redissonClient.getBloomFilter(key);
    bloomFilter.tryInit(
        expectedInsertions == null ? 10000L : expectedInsertions,
        falseProbability == null ? 0.01D : falseProbability);
    log.info(
        "블룸 필터 '{}' 초기화 완료 - 예상 데이터 개수: {}, False Positive: {}",
        key,
        expectedInsertions,
        falseProbability);
  }

  public boolean add(String data) {
    if (data == null) {
      return false;
    }
    return bloomFilter.add(data);
  }

  public boolean contains(String data) {
    if (data == null) {
      return false;
    }
    return bloomFilter.contains(data);
  }
}
