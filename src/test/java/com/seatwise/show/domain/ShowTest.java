package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ShowTest {

  @Nested
  @DisplayName("공연 시간이 겹치는 경우")
  class OverlappingScheduleTests {
    @Test
    @DisplayName("기존 공연의 종료 시각이 새로운 공연의 시작 시각과 겹치면 중복이다")
    void isOverlapping_WithEndTimeEqualToStartTime_ReturnsTrue() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(13, 50), LocalTime.of(15, 0));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isTrue();
    }

    @Test
    @DisplayName("기존 공연의 시작 시각이 새로운 공연의 종료 시각과 겹치면 중복이다")
    void isOverlapping_WithStartTimeEqualToEndTime_ReturnsTrue() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(12, 10));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isTrue();
    }

    @Test
    @DisplayName("기존 공연의 시작 시각과 종료 시각이 새로운 공연과 모두 겹치면 중복이다")
    void isOverlapping_WithinExistingSchedule_ReturnsTrue() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 30), LocalTime.of(13, 50));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isTrue();
    }

    @Test
    @DisplayName("새로운 공연 시간이 기존 공연 시간을 완전히 포함하면 중복이다")
    void isOverlapping_WithExistingScheduleWithinNew_ReturnsTrue() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(15, 0));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isTrue();
    }
  }

  @Nested
  @DisplayName("공연 시간이 겹치지 않는 경우")
  class NonOverlappingScheduleTests {
    @Test
    @DisplayName("기존 공연이 끝난 후 새로운 공연이 시작하면 중복이 아니다")
    void isOverlapping_WithEndTimeBeforeStartTime_ReturnsFalse() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 30), LocalTime.of(16, 0));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isFalse();
    }

    @Test
    @DisplayName("기존 공연이 시작하기 전에 새로운 공연이 끝나면 중복이 아니다")
    void isOverlapping_WithStartTimeAfterEndTime_ReturnsFalse() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(14, 0), LocalTime.of(16, 0));
      Show newShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(13, 0));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isFalse();
    }

    @Test
    @DisplayName("시간이 겹쳐도 날짜가 다르면 중복이 아니다")
    void isOverlapping_WithTimeOverlappingButNotSameDate_ReturnsFalse() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(
              null, null, LocalDate.of(2024, 1, 2), LocalTime.of(12, 30), LocalTime.of(13, 50));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isFalse();
    }

    @Test
    @DisplayName("같은 날짜에 시간이 완전히 겹치지 않으면 중복이 아니다")
    void isOverlapping_WithSameDateAndNotOverlappingTime_ReturnsFalse() {
      // given
      Show existingShow =
          new Show(null, null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));
      Show newShow =
          new Show(
              null, null, LocalDate.of(2024, 1, 1), LocalTime.of(15, 30), LocalTime.of(16, 50));

      // when & then
      assertThat(existingShow.isOverlapping(newShow)).isFalse();
    }
  }

  @Nested
  @DisplayName("공연 시간 유효성 검사")
  class ValidationTests {
    @Test
    @DisplayName("공연 종료 시각이 시작 시각보다 이르면 예외가 발생한다")
    void validateTimes_WithEndTimeBeforeStartTime_ThrowsBadRequestException() {
      // given
      LocalTime startTime = LocalTime.of(16, 0);
      LocalTime endTime = LocalTime.of(14, 0);
      LocalDate date = LocalDate.now();

      // when & then
      assertThatThrownBy(() -> new Show(null, null, date, startTime, endTime))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(ErrorCode.INVALID_SHOW_TIME.getMessage());
    }
  }
}
