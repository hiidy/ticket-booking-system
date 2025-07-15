package com.seatwise.booking.messaging.rebalancer;

import java.util.List;

public record StreamConsumerState(String consumerId, List<Integer> partitions) {}
