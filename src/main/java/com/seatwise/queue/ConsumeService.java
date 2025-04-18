package com.seatwise.queue;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumeService
    implements StreamListener<String, ObjectRecord<String, ProduceRequest>> {

  @Value("${booking.instance-id:${random.uuid}}")
  private String instanceId;

  private final StreamMessageListenerContainer<String, ObjectRecord<String, ProduceRequest>>
      container;
  private final RedisTemplate<String, Object> redisTemplate;
  private final QueueProperties queueProperties;

  @PostConstruct
  public void init() {
    int instanceHash = instanceId.hashCode();
    int shardCount = queueProperties.getShardCount();
    int instanceCount = queueProperties.getInstanceCount();
    String group = queueProperties.getConsumerGroup();

    for (int shardId = 0; shardId < shardCount; shardId++) {
      if (shardId % instanceCount == instanceHash % instanceCount) {
        String streamKey = StreamKeyGenerator.createStreamKey(shardId);

        try {
          redisTemplate.opsForStream().createGroup(streamKey, group);
        } catch (Exception e) {
          log.info("해당 스트림키에 대해서 그룹이 이미 존재합니다 : {}", streamKey);
        }

        container.receive(
            Consumer.from(group, instanceId),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            this);
      }
    }
    container.start();
  }

  @PreDestroy
  public void destroy() {
    if (container != null) {
      container.stop();
    }
  }

  @Override
  public void onMessage(ObjectRecord<String, ProduceRequest> message) {
    ProduceRequest request = message.getValue();
    log.info(
        "멤버Id: {}, 좌석Id: {}, 섹션Id: {}에 대한 요청 처리중",
        request.memberId(),
        request.showSeatIds(),
        request.sectionId());
  }
}
