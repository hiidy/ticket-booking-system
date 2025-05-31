package com.seatwise.showtime.service;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.ShowType;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.entity.ShowTime;
import com.seatwise.showtime.repository.ShowTimeRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class ShowTimeServiceTest {

  @Autowired private ShowTimeService showTimeService;
  @Autowired private ShowTimeRepository showTimeRepository;
  @Autowired private ShowRepository showRepository;
  @Autowired private VenueRepository venueRepository;

  private Show show;
  private Venue venue;

  @BeforeEach
  void setUp() {
    show = new Show("지킬 앤 하이드", "테스트 공연", ShowType.MUSICAL);
    showRepository.save(show);
    venue = new Venue("test", 100);
    venueRepository.save(venue);
  }

  @Test
  @DisplayName("show를 만들때 시간이 겹치면 예외 반환")
  void createShow_WithOverlappingTime_ThrowsException() {
    // given
    ShowTime existingShowTime =
        new ShowTime(
            show, venue, LocalDate.of(2024, 1, 1), LocalTime.of(15, 0), LocalTime.of(17, 0));
    showTimeRepository.save(existingShowTime);

    ShowTimeCreateRequest request =
        new ShowTimeCreateRequest(
            show.getId(),
            venue.getId(),
            LocalDate.of(2024, 1, 1),
            LocalTime.of(16, 0),
            LocalTime.of(17, 0));

    // when & then
    assertThatThrownBy(() -> showTimeService.createShow(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.DUPLICATE_SHOW.getMessage());
  }
}
