package com.seatwise.booking.messaging.rebalancer;

public record RebalanceMessage(RebalanceType rebalanceType, String requestedBy) {}
