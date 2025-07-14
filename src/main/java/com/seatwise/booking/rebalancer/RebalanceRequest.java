package com.seatwise.booking.rebalancer;

public record RebalanceRequest(RebalanceType rebalanceType, String requestedBy) {}
