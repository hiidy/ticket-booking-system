package com.seatwise.booking.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartitionCalculator {

  private static final int DEFAULT_PARTITION_COUNT = 32;

  public static int calculatePartition(Long sectionId, int partitionCount) {
    int validPartitionCount = (partitionCount > 0) ? partitionCount : DEFAULT_PARTITION_COUNT;
    return (int) (sectionId % validPartitionCount);
  }
}
