package com.seatwise.showtime.domain;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowRepository;
import com.seatwise.show.domain.ShowType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShowTimeRepositoryTest {

  @Autowired ShowTimeRepository showTimeRepository;
  @Autowired ShowRepository showRepository;

  @Test
  @DisplayName("특정 기간의 공연 날짜를 조회할 수 있다.")
  void findShowsInDateRange() {
    // given
    Show show = new Show("지킬 앤 하이드", "테스트 공연", ShowType.MUSICAL);
    showRepository.save(show);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(13, 0);
    LocalTime endTime = startTime.plusHours(1);

    ShowTime showTime1 = new ShowTime(show, null, date, startTime, endTime);
    ShowTime showTime2 = new ShowTime(show, null, date.plusDays(2), startTime, endTime);
    ShowTime showTime3 = new ShowTime(show, null, date.plusDays(3), startTime, endTime);
    showTimeRepository.saveAll(List.of(showTime1, showTime2, showTime3));

    // when
    List<ShowTime> datesInRange =
        showTimeRepository.findByShowIdAndDateGreaterThanEqualAndDateLessThan(
            show.getId(), date, date.plusDays(3));

    // then
    assertThat(datesInRange).hasSize(2);
  }

  @Test
  @DisplayName("특정 이벤트의 특정 날짜에 예정된 공연 목록을 조회한다.")
  void findShowsByEventIdAndDate() {
    // Given
    Show show = new Show("지킬 앤 하이드", "테스트 공연", ShowType.MUSICAL);
    showRepository.save(show);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime time = LocalTime.of(13, 0);

    ShowTime showTime1 = new ShowTime(show, null, date, time, time.plusHours(1));
    ShowTime showTime2 = new ShowTime(show, null, date, time.plusHours(2), time.plusHours(3));
    ShowTime showTime3 =
        new ShowTime(show, null, date.plusMonths(1), time.plusHours(2), time.plusHours(3));
    showTimeRepository.saveAll(List.of(showTime1, showTime2, showTime3));

    // when
    List<ShowTime> showTimes =
        showTimeRepository.findShowTimesByShowIdAndDate(show.getId(), LocalDate.of(2025, 1, 1));

    // then
    assertThat(showTimes).hasSize(2);
  }
}
