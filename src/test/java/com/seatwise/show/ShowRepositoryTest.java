package com.seatwise.show;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.event.domain.Event;
import com.seatwise.show.domain.Show;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ShowRepositoryTest {

  @Autowired ShowRepository showRepository;
  @Autowired TestEntityManager em;

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
    em.persist(show1);
    em.flush();

    Show show2 =
        Show.builder()
            .date(LocalDate.now().plusDays(3))
            .startTime(LocalTime.now())
            .endTime(LocalTime.now().plusHours(1))
            .event(event)
            .build();
    em.persist(show2);
    em.flush();

    Show show3 =
        Show.builder()
            .date(LocalDate.now().plusDays(4))
            .startTime(LocalTime.now())
            .endTime(LocalTime.now().plusHours(1))
            .event(event)
            .build();
    em.persist(show3);
    em.flush();

    // When
    List<LocalDate> datesInRange =
        showRepository.findShowDatesByEventIdAndDateBetween(
            event.getId(), LocalDate.now(), LocalDate.now().plusDays(3));

    // Then
    assertThat(datesInRange).hasSize(2);
  }
}
