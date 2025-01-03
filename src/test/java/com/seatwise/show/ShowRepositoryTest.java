package com.seatwise.show;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.RepositoryTest;
import com.seatwise.event.domain.Event;
import com.seatwise.show.domain.Show;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowRepositoryTest extends RepositoryTest {

  @Autowired ShowRepository showRepository;

  @Test
  @DisplayName("특정 기간의 공연 날짜를 조회 할 수 있다.")
  void testFindShowDatesByEventIdAndDateBetween() {
    // Given
    Event event = Event.builder().title("공연1").build();
    em.persist(event);

    Show show1 =
        Show.builder()
            .date(LocalDate.now())
            .startTime(LocalTime.now())
            .endTime(LocalTime.now().plusHours(1))
            .event(event)
            .build();
    persistAndFlush(show1);

    Show show2 =
        Show.builder()
            .date(LocalDate.now().plusDays(3))
            .startTime(LocalTime.now())
            .endTime(LocalTime.now().plusHours(1))
            .event(event)
            .build();
    persistAndFlush(show2);

    Show show3 =
        Show.builder()
            .date(LocalDate.now().plusDays(4))
            .startTime(LocalTime.now())
            .endTime(LocalTime.now().plusHours(1))
            .event(event)
            .build();
    persistAndFlush(show3);

    // When
    List<LocalDate> datesInRange =
        showRepository.findShowDatesByEventIdAndDateBetween(
            event.getId(), LocalDate.now(), LocalDate.now().plusDays(3));

    // Then
    assertThat(datesInRange).hasSize(2);
  }

  @Test
  @DisplayName("특정 이벤트의 특정 날짜에 예정된 공연 목록을 조회한다.")
  void testFindShowsByEventIdAndDate() {
    // Given
    Event event = Event.builder().title("공연1").build();
    em.persist(event);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime time = LocalTime.of(13, 0);

    Show show1 =
        Show.builder().date(date).startTime(time).endTime(time.plusHours(1)).event(event).build();
    persistAndFlush(show1);

    Show show2 =
        Show.builder()
            .date(date)
            .startTime(time.plusHours(2))
            .endTime(time.plusHours(3))
            .event(event)
            .build();
    persistAndFlush(show2);

    Show show3 =
        Show.builder()
            .date(date.plusMonths(1))
            .startTime(time.plusHours(2))
            .endTime(time.plusHours(3))
            .event(event)
            .build();
    persistAndFlush(show3);

    // When
    List<Show> shows =
        showRepository.findShowsByEventIdAndDate(event.getId(), LocalDate.of(2025, 1, 1));

    // Then
    assertThat(shows).hasSize(2);
  }
}
