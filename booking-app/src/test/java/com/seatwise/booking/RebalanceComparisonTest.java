package com.seatwise.booking;

import com.seatwise.booking.messaging.rebalancer.ConsistentHash;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class RebalanceComparisonTest {

  static class ModularHash {
    private List<String> nodes = new ArrayList<>();

    public void addNode(String node) {
      nodes.add(node);
    }

    public void removeNode(String node) {
      nodes.remove(node);
    }

    public String get(String key) {
      int hash = Math.abs(key.hashCode());
      return nodes.get(hash % nodes.size());
    }
  }

  @Test
  void testRebalance() {
    List<String> streamPartitions = new ArrayList<>();
    int partitionCount = 32;

    for (int partitionId = 0; partitionId < partitionCount; partitionId++) {
      streamPartitions.add(String.format("booking:partition:%d", partitionId));
    }

    System.out.println("Redis Stream 파티션 Consumer 서버 매칭 리밸런싱 비교");
    System.out.println("총 Stream 파티션 수: " + streamPartitions.size());
    System.out.println("파티션 키 패턴: booking:partition:{id}");
    System.out.println();

    double totalModularRate = 0;
    double totalConsistentRate = 0;

    for (int initialConsumers = 1; initialConsumers <= 32; initialConsumers++) {
      double[] rates = testConsumerRebalancing(streamPartitions, initialConsumers);
      totalModularRate += rates[0];
      totalConsistentRate += rates[1];
    }

    System.out.println("\n=== 평균 재배치율 ===");
    System.out.printf("모듈러 연산 평균: %.2f%%\n", totalModularRate / 32);
    System.out.printf("Consistent Hash 평균: %.2f%%\n", totalConsistentRate / 32);
  }

  private double[] testConsumerRebalancing(List<String> streamPartitions, int initialConsumers) {
    System.out.printf("Consumer 서버 수: %d개 → %d개로 확장\n", initialConsumers, initialConsumers + 1);

    // 모듈러 기반
    ModularHash modularBefore = new ModularHash();
    ModularHash modularAfter = new ModularHash();

    for (int i = 0; i < initialConsumers; i++) {
      String consumerId = String.format("consumer-server-%d-8080", i);
      modularBefore.addNode(consumerId);
      modularAfter.addNode(consumerId);
    }

    Map<String, String> modularMapping = new HashMap<>();
    for (String streamPartition : streamPartitions) {
      modularMapping.put(streamPartition, modularBefore.get(streamPartition));
    }

    String newConsumer = String.format("consumer-server-%d-8080", initialConsumers);
    modularAfter.addNode(newConsumer);

    int modularRelocated = 0;
    for (String streamPartition : streamPartitions) {
      String before = modularMapping.get(streamPartition);
      String after = modularAfter.get(streamPartition);
      if (!before.equals(after)) {
        modularRelocated++;
      }
    }

    // ConsistentHash 기반
    int virtualNodes = 100;
    ConsistentHash<String> consistentBefore =
        new ConsistentHash<>(virtualNodes, Collections.emptyList());
    ConsistentHash<String> consistentAfter =
        new ConsistentHash<>(virtualNodes, Collections.emptyList());

    for (int i = 0; i < initialConsumers; i++) {
      String consumerId = String.format("consumer-server-%d-8080", i);
      consistentBefore.add(consumerId);
      consistentAfter.add(consumerId);
    }

    Map<String, String> consistentMapping = new HashMap<>();
    for (String streamPartition : streamPartitions) {
      consistentMapping.put(streamPartition, consistentBefore.get(streamPartition));
    }

    consistentAfter.add(newConsumer);

    int consistentRelocated = 0;
    for (String streamPartition : streamPartitions) {
      String before = consistentMapping.get(streamPartition);
      String after = consistentAfter.get(streamPartition);
      if (!Objects.equals(before, after)) {
        consistentRelocated++;
      }
    }

    double modularRate = (double) modularRelocated / streamPartitions.size() * 100;
    double consistentRate = (double) consistentRelocated / streamPartitions.size() * 100;
    double improvement = ((modularRate - consistentRate) / modularRate) * 100;
    double theoreticalRate = 100.0 / (initialConsumers + 1);

    System.out.printf("기존 모듈러 연산: %.1f%% (%d개 파티션 재배치)\n", modularRate, modularRelocated);
    System.out.printf(
        "Consistent Hash: %.1f%% (%d개 파티션 재배치)\n", consistentRate, consistentRelocated);
    System.out.printf("개선 효과:%.1f%% 감소\n", improvement);
    System.out.printf("이론적 최적값: %.1f%%\n", theoreticalRate);
    System.out.println("=".repeat(50));

    return new double[] {modularRate, consistentRate};
  }
}
