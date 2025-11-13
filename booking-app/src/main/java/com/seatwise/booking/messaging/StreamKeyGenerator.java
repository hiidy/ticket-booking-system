package com.seatwise.booking.messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamKeyGenerator {
  private static final String BOOKING_PARTITION_KEY = "booking:partition:%d";
  private static final String REBALANCE_UPDATE_KEY = "rebalance:updates";

  public static String createStreamKey(int partitionId) {
    return String.format(BOOKING_PARTITION_KEY, partitionId);
  }

  public static String getRebalanceUpdateKey() {
    return REBALANCE_UPDATE_KEY;
  }
}
