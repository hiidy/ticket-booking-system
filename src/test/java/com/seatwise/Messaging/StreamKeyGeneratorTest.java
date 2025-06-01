package com.seatwise.Messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StreamKeyGeneratorTest {

  @Test
  void givenLargeId_whenGenerateStreamKey_thenSuccess() {
    // given
    Long sectionId = 1233232323223456789L;
    int totalShard = 5;

    // when
    String streamKey1 = StreamKeyGenerator.forSectionShard(sectionId, totalShard);

    // then
    assertThat(streamKey1).isEqualTo("section:shard:4:stream");
  }
}
