package com.seatwise.show.service;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ShowServiceTest {

  @Autowired private ShowService showService;
  @Autowired private ShowRepository showRepository;
  @Autowired private EventRepository eventRepository;

  private Event event;

  @BeforeEach
  void setUp() {
    event = new Event("지킬 앤 하이드", "테스트 공연", EventType.MUSICAL);
    eventRepository.save(event);
  }

  @Test
  @DisplayName("show를 만들때 시간이 겹치면 예외 반환")
  void createShow_WithOverlappingTime_ThrowsException() {
    // given
    Show existingShow =
        new Show(event, LocalDate.of(2024, 1, 1), LocalTime.of(15, 0), LocalTime.of(17, 0));
    showRepository.save(existingShow);

    ShowCreateRequest request =
        new ShowCreateRequest(
            1L, LocalDate.of(2024, 1, 1), LocalTime.of(16, 0), LocalTime.of(17, 0));

    // when & then
    assertThatThrownBy(() -> showService.createShow(request))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.DUPLICATE_SHOW.getMessage());
  }
}
