package com.seatwise.showtime.service;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowRepository;
import com.seatwise.show.domain.ShowType;
import com.seatwise.showtime.ShowTimeService;
import com.seatwise.showtime.domain.ShowTime;
import com.seatwise.showtime.domain.ShowTimeRepository;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.dto.response.ShowTimeSummaryResponse;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.domain.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
  void createShowTime_WithOverlappingTime_ThrowsException() {
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
    assertThatThrownBy(() -> showTimeService.createShowTime(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.DUPLICATE_SHOW.getMessage());
  }

  @Test
  void shouldReturnOnlyShowDatesWithinSpecifiedMonth() {
    // given
    LocalDate may31 = LocalDate.of(2025, 5, 31);
    LocalDate june15 = LocalDate.of(2025, 6, 15);
    LocalDate june30 = LocalDate.of(2025, 6, 30);
    LocalDate july1 = LocalDate.of(2025, 7, 1);

    showTimeRepository.saveAll(
        List.of(
            new ShowTime(show, venue, may31, LocalTime.of(10, 0), LocalTime.of(12, 0)),
            new ShowTime(show, venue, june15, LocalTime.of(10, 0), LocalTime.of(12, 0)),
            new ShowTime(show, venue, june30, LocalTime.of(14, 0), LocalTime.of(16, 0)),
            new ShowTime(show, venue, july1, LocalTime.of(10, 0), LocalTime.of(12, 0))));

    // when
    List<ShowTimeSummaryResponse> results =
        showTimeService.getAvailableDates(show.getId(), 2025, 6);

    // then
    assertThat(results)
        .hasSize(2)
        .allSatisfy(resp -> assertThat(resp.date().getMonthValue()).isEqualTo(6));
  }
}
