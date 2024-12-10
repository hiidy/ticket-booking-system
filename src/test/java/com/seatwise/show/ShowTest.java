package com.seatwise.show;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.global.exception.BadRequestException;
import com.seatwise.global.exception.ErrorCode;
import com.seatwise.show.domain.Show;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowTest {

  @Test
  @DisplayName("새로운 show 시간이 기존 show의 종료 시간과 겹치면 중복으로 판단한다")
  void validateOverlappingWithEndTime() {
    // Given
    Show show1 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

    Show show2 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(13, 50))
            .endTime(LocalTime.of(15, 0))
            .build();

    // When & Then
    assertThat(show1.isOverlapping(show2)).isTrue();
  }

  @Test
  @DisplayName("새로운 show 시간이 기존 show의 시작 시간과 겹치면 중복으로 판단한다")
  void validateOverlappingWithStartTime() {
    // Given
    Show show1 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

    Show show2 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(11, 0))
            .endTime(LocalTime.of(12, 10))
            .build();

    // When & Then
    assertThat(show1.isOverlapping(show2)).isTrue();
  }

  @Test
  @DisplayName("새로운 show 시간이 기존 show과 겹치지 않으면 중복이 아니다")
  void validateNotOverlappingTime() {
    // Given
    Show show1 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

    Show show2 =
        Show.builder()
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(16, 0))
            .endTime(LocalTime.of(17, 10))
            .build();

    // When & Then
    assertThat(show1.isOverlapping(show2)).isFalse();
  }

  @Test
  @DisplayName("show의 종료 시간이 시작 시간 이후인지 확인")
  void validateStartTimeIsBeforeEndTime() {

    // Given
    LocalTime startTime = LocalTime.of(16, 0);
    LocalTime endTime = LocalTime.of(14, 0);

    // When & Then
    assertThatThrownBy(() -> Show.builder().startTime(startTime).endTime(endTime).build())
        .isInstanceOf(BadRequestException.class)
        .hasMessage(ErrorCode.INVALID_SHOW_TIME.getMessage());
  }
}
