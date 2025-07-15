package com.seatwise.booking.messaging.rebalancer;

public record RebalanceRequest(RebalanceType rebalanceType, String requestedBy) {}
