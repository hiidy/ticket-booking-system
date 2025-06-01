package com.seatwise.Messaging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamKeyGenerator {
  private static final String STREAM_KEY_FORMAT = "section:shard:%d:stream";
  private static final int DEFAULT_SHARD_COUNT = 32;

  public static String forSectionShard(Long sectionId, int requestShardCount) {
    int shardCount = (requestShardCount > 0) ? requestShardCount : DEFAULT_SHARD_COUNT;
    int shardId = (int) ((sectionId) % shardCount);
    return createStreamKey(shardId);
  }

  public static String createStreamKey(int shardId) {
    return String.format(STREAM_KEY_FORMAT, shardId);
  }
}
