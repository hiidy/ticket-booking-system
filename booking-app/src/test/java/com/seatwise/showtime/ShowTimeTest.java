package com.seatwise.showtime;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.core.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.entity.ShowTime;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShowTimeTest {

  @Nested
  @DisplayName("When showtimes overlap")
  class OverlappingScheduleTests {

    @Test
    void shouldReturnTrue_whenNewStartTimeIsBeforeExistingEndTime() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(13, 50), LocalTime.of(15, 0));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewEndTimeIsAfterExistingStartTime() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(12, 10));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewTimeIsInsideExistingShowTime() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 30), LocalTime.of(13, 50));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenNewShowTimeFullyEnclosesExisting() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(15, 0));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isTrue();
    }
  }

  @Nested
  @DisplayName("When showtimes do not overlap")
  class NonOverlappingScheduleTests {

    @Test
    void shouldReturnFalse_whenNewStartTimeIsAfterExistingEndTime() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 30), LocalTime.of(16, 0));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenNewEndTimeIsBeforeExistingStartTime() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 0), LocalTime.of(16, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(13, 0));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    @DisplayName("should return false when date differs even if times overlap")
    void shouldReturnFalse_whenDateIsDifferentEvenIfTimeOverlaps() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 2), LocalTime.of(12, 30), LocalTime.of(13, 50));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenTimesDoNotOverlapOnSameDate() {
      ShowTime existingShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      ShowTime newShowTime =
          new ShowTime(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(15, 30), LocalTime.of(16, 50));

      assertThat(existingShowTime.isOverlapping(newShowTime)).isFalse();
    }
  }

  @Nested
  @DisplayName("Validation for showtime creation")
  class ValidationTests {

    @Test
    void shouldThrowException_whenEndTimeIsBeforeStartTime() {
      LocalTime startTime = LocalTime.of(16, 0);
      LocalTime endTime = LocalTime.of(14, 0);
      LocalDate date = LocalDate.now();

      assertThatThrownBy(() -> new ShowTime(null, null, date, startTime, endTime))
          .isInstanceOf(BusinessException.class)
          .hasMessage(BaseCode.INVALID_SHOW_TIME.getMessage());
    }
  }
}
