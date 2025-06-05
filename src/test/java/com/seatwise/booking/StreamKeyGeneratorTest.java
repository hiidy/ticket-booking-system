package com.seatwise.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.messaging.StreamKeyGenerator;
import org.junit.jupiter.api.Test;

class StreamKeyGeneratorTest {

  @Test
  void shouldReturnCorrectShardStreamKey_whenGivenLargeSectionIdAndShardCount() {
    // given
    Long sectionId = 1233232323223456789L;
    int totalShard = 5;

    // when
    String streamKey = StreamKeyGenerator.forSectionShard(sectionId, totalShard);

    // then
    assertThat(streamKey).isEqualTo("section:shard:4:stream");
  }
}
