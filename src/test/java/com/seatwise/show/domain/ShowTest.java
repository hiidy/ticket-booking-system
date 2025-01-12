package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShowTest {

  @Test
  @DisplayName("새로운 show 시간이 기존 show의 종료 시간과 겹치면 중복으로 판단한다")
  void validateOverlappingWithEndTime() {
    // Given
    Show show1 = new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));

    Show show2 =
        new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(13, 50), LocalTime.of(15, 0));

    // When & Then
    assertThat(show1.isOverlapping(show2)).isTrue();
  }

  @Test
  @DisplayName("새로운 show 시간이 기존 show의 시작 시간과 겹치면 중복으로 판단한다")
  void validateOverlappingWithStartTime() {
    // Given
    Show show1 = new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));

    Show show2 =
        new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(11, 0), LocalTime.of(12, 10));

    // When & Then
    assertThat(show1.isOverlapping(show2)).isTrue();
  }

  @Test
  @DisplayName("새로운 show 시간이 기존 show과 겹치지 않으면 중복이 아니다")
  void validateNotOverlappingTime() {
    // Given
    Show show1 = new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), LocalTime.of(14, 0));

    Show show2 =
        new Show(null, LocalDate.of(2024, 1, 1), LocalTime.of(16, 0), LocalTime.of(17, 10));

    // When & Then
    assertThat(show1.isOverlapping(show2)).isFalse();
  }

  @Test
  @DisplayName("show의 종료 시간이 시작 시간 이후인지 확인")
  void validateStartTimeIsBeforeEndTime() {
    // Given
    LocalTime startTime = LocalTime.of(16, 0);
    LocalTime endTime = LocalTime.of(14, 0);
    LocalDate date = LocalDate.now();

    // When & Then
    assertThatThrownBy(
            () -> {
              Show show = new Show(null, date, startTime, endTime);
            })
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.INVALID_SHOW_TIME.getMessage());
  }
}
