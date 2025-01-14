package com.seatwise.show.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.RepositoryTest;
import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ShowRepositoryTest extends RepositoryTest {

  @Autowired ShowRepository showRepository;
  @Autowired EventRepository eventRepository;

  @Test
  @DisplayName("특정 기간의 공연 날짜를 조회할 수 있다.")
  void findShowsInDateRange() {
    // given
    Event event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    eventRepository.save(event);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(13, 0);
    LocalTime endTime = startTime.plusHours(1);

    Show show1 = new Show(event, date, startTime, endTime);
    Show show2 = new Show(event, date.plusDays(3), startTime, endTime);
    Show show3 = new Show(event, date.plusDays(4), startTime, endTime);
    showRepository.saveAll(List.of(show1, show2, show3));

    // when
    List<Show> datesInRange =
        showRepository.findByEventIdAndDateBetween(event.getId(), date, date.plusDays(3));

    // then
    assertThat(datesInRange).hasSize(2);
  }

  @Test
  @DisplayName("특정 이벤트의 특정 날짜에 예정된 공연 목록을 조회한다.")
  void findShowsByEventIdAndDate() {
    // Given
    Event event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    eventRepository.save(event);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime time = LocalTime.of(13, 0);

    Show show1 = new Show(event, date, time, time.plusHours(1));
    Show show2 = new Show(event, date, time.plusHours(2), time.plusHours(3));
    Show show3 = new Show(event, date.plusMonths(1), time.plusHours(2), time.plusHours(3));
    showRepository.saveAll(List.of(show1, show2, show3));

    // when
    List<Show> shows =
        showRepository.findShowsByEventIdAndDate(event.getId(), LocalDate.of(2025, 1, 1));

    // then
    assertThat(shows).hasSize(2);
  }
}
