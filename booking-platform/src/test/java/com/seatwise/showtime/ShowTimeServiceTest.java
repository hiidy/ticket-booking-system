package com.seatwise.showtime;

import static org.assertj.core.api.Assertions.*;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.show.Show;
import com.seatwise.show.ShowRepository;
import com.seatwise.show.ShowType;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.dto.response.ShowTimeSummaryResponse;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
  void shouldThrowException_whenShowTimeOverlaps() {
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
  void shouldReturnOnlyShowDates_whenQueryingByYearAndMonth() {
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
