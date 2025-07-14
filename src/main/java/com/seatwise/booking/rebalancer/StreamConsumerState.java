package com.seatwise.booking.rebalancer;

import java.util.List;

public record StreamConsumerState(String consumerId, List<Integer> partitions) {}
