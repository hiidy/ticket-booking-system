package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.RepositoryTest;
import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
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
    Event event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    em.persist(event);

    Show show1 = new Show(event, LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(1));
    persistAndFlush(show1);

    Show show2 =
        new Show(event, LocalDate.now().plusDays(3), LocalTime.now(), LocalTime.now().plusHours(1));
    persistAndFlush(show2);

    Show show3 =
        new Show(event, LocalDate.now().plusDays(4), LocalTime.now(), LocalTime.now().plusHours(1));
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
    Event event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    em.persist(event);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime time = LocalTime.of(13, 0);

    Show show1 = new Show(event, date, time, time.plusHours(1));
    persistAndFlush(show1);

    Show show2 = new Show(event, date, time.plusHours(2), time.plusHours(3));
    persistAndFlush(show2);

    Show show3 = new Show(event, date.plusMonths(1), time.plusHours(2), time.plusHours(3));
    persistAndFlush(show3);

    // When
    List<Show> shows =
        showRepository.findShowsByEventIdAndDate(event.getId(), LocalDate.of(2025, 1, 1));

    // Then
    assertThat(shows).hasSize(2);
  }
}
