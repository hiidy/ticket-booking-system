package com.seatwise.booking.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamKeyGenerator {
  private static final String STREAM_KEY_FORMAT = "booking:partition:%d";
  private static final int DEFAULT_PARTITION_COUNT = 32;

  public static String forSectionPartition(Long sectionId, int requestPartitionCount) {
    int partitionCount =
        (requestPartitionCount > 0) ? requestPartitionCount : DEFAULT_PARTITION_COUNT;
    int partitionId = (int) ((sectionId) % partitionCount);
    return createStreamKey(partitionId);
  }

  public static String createStreamKey(int partitionId) {
    return String.format(STREAM_KEY_FORMAT, partitionId);
  }
}
