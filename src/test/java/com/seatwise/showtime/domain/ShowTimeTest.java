package com.seatwise.showtime.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShowTimeTest {

  @Nested
  @DisplayName("공연 시간이 겹치는 경우")
  class OverlappingScheduleTests {

    @Test
    void shouldReturnTrue_whenNewStartTimeIsBeforeExistingEndTime() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(13, 50), LocalTime.of(15, 0));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewEndTimeIsAfterExistingStartTime() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(12, 10));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewTimeIsInsideExistingShowTime() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 30), LocalTime.of(13, 50));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewShowTimeFullyEnclosesExisting() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(15, 0));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }
  }

  @Nested
  @DisplayName("공연 시간이 겹치지 않는 경우")
  class NonOverlappingScheduleTests {

    @Test
    void shouldReturnFalse_whenNewStartTimeIsAfterExistingEndTime() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 30), LocalTime.of(16, 0));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenNewEndTimeIsBeforeExistingStartTime() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 0), LocalTime.of(16, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(13, 0));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    @DisplayName("should return false when date is different even if time overlaps")
    void shouldReturnFalse_whenDateIsDifferentEvenIfTimeOverlaps() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 2), LocalTime.of(12, 30), LocalTime.of(13, 50));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTimesDoNotOverlapOnSameDate() {
      // given
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(15, 30), LocalTime.of(16, 50));

      // when & then
      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }
  }

  @Nested
  @DisplayName("공연 시간 유효성 검사")
  class ValidationTests {

    @Test
    void shouldThrowException_whenEndTimeIsBeforeStartTime() {
      // given
      LocalTime startTime = LocalTime.of(16, 0);
      LocalTime endTime = LocalTime.of(14, 0);
      LocalDate date = LocalDate.now();

      // when & then
      assertThatThrownBy(() -> new ShowTime(null, null, date, startTime, endTime))
          .isInstanceOf(BusinessException.class)
          .hasMessage(ErrorCode.INVALID_SHOW_TIME.getMessage());
    }
  }
}
