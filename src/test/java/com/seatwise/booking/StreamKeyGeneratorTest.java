package com.seatwise.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.booking.messaging.PartitionCalculator;
import com.seatwise.booking.messaging.StreamKeyGenerator;
import org.junit.jupiter.api.Test;

class StreamKeyGeneratorTest {

  @Test
  void shouldReturnCorrectStreamKey_whenGivenLargeSectionIdAndPartitionCount() {
    // given
    Long sectionId = 1233232323223456789L;
    int totalPartition = 5;

    // when
    String streamKey =
        StreamKeyGenerator.createStreamKey(
            PartitionCalculator.calculatePartition(sectionId, totalPartition));

    // then
    assertThat(streamKey).isEqualTo("booking:partition:4");
  }
}
