package com.seatwise.booking.messaging.rebalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RebalanceMessageConsumer
    implements StreamListener<String, ObjectRecord<String, RebalanceMessage>> {

  private final RebalanceCoordinator rebalanceCoordinator;

  @Override
  public void onMessage(ObjectRecord<String, RebalanceMessage> message) {}
}
