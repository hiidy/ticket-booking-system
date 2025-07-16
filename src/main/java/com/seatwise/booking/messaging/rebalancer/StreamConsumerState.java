package com.seatwise.booking.messaging.rebalancer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StreamConsumerState {

  private final String consumerId;
  private final List<Integer> partitions;

  @JsonCreator
  public StreamConsumerState(
      @JsonProperty("consumerId") String consumerId,
      @JsonProperty("partitions") List<Integer> partitions) {
    this.consumerId = consumerId;
    this.partitions = new ArrayList<>(partitions);
  }

  public boolean updatePartitions(List<Integer> newPartitions) {
    if (partitions.equals(newPartitions)) {
      return false;
    }

    partitions.clear();
    partitions.addAll(newPartitions);

    return true;
  }

  public List<Integer> getPartitions() {
    return Collections.unmodifiableList(partitions);
  }
}
