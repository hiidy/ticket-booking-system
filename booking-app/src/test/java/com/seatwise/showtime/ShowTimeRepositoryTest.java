package com.seatwise.showtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.show.entity.Show;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.entity.ShowTime;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.repository.ShowTimeRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ShowTimeRepositoryTest {

  @Autowired
  ShowTimeRepository showTimeRepository;
  @Autowired ShowRepository showRepository;

  @Test
  void shouldFindShowDatesWithinRange_whenDateIsBetweenStartAndEnd() {
    // given
    Show show = new Show("지킬 앤 하이드", "테스트 공연", ShowType.MUSICAL);
    showRepository.save(show);

    LocalDate baseDate = LocalDate.of(2025, 1, 1);
    LocalTime startTime = LocalTime.of(13, 0);
    LocalTime endTime = startTime.plusHours(1);

    ShowTime showTime1 = new ShowTime(show, null, baseDate, startTime, endTime); // 1/1
    ShowTime showTime2 = new ShowTime(show, null, baseDate.plusDays(2), startTime, endTime); // 1/3
    ShowTime showTime3 = new ShowTime(show, null, baseDate.plusDays(3), startTime, endTime); // 1/4
    showTimeRepository.saveAll(List.of(showTime1, showTime2, showTime3));

    // when
    List<ShowTime> datesInRange =
        showTimeRepository.findByShowIdAndDateGreaterThanEqualAndDateLessThan(
            show.getId(), baseDate, baseDate.plusDays(3)); // [1/1, 1/3) → 1/1, 1/3 포함 안됨

    // then
    assertThat(datesInRange).hasSize(2);
  }

  @Test
  void shouldFindShowTimes_whenQueryingByShowIdAndExactDate() {
    // given
    Show show = new Show("지킬 앤 하이드", "테스트 공연", ShowType.MUSICAL);
    showRepository.save(show);

    LocalDate date = LocalDate.of(2025, 1, 1);
    LocalTime time = LocalTime.of(13, 0);

    ShowTime showTime1 = new ShowTime(show, null, date, time, time.plusHours(1)); // 1/1 13-14
    ShowTime showTime2 =
        new ShowTime(show, null, date, time.plusHours(2), time.plusHours(3)); // 1/1 15-16
    ShowTime showTime3 =
        new ShowTime(show, null, date.plusMonths(1), time, time.plusHours(1)); // 2/1
    showTimeRepository.saveAll(List.of(showTime1, showTime2, showTime3));

    // when
    List<ShowTime> showTimes = showTimeRepository.findShowTimesByShowIdAndDate(show.getId(), date);

    // then
    assertThat(showTimes).hasSize(2);
  }
}
