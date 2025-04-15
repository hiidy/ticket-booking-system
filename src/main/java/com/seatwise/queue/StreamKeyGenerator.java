package com.seatwise.queue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamKeyGenerator {
  private static final String STREAM_KEY_FORMAT = "section:shard:%d:stream";

  public static String forSectionShard(Long sectionId, int totalShard) {
    int shardId = (int) ((sectionId) % totalShard);
    return String.format(STREAM_KEY_FORMAT, shardId);
  }
}
