package com.seatwise.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.venue.SeatService;
import com.seatwise.venue.dto.request.SeatCreateRequest;
import com.seatwise.venue.dto.request.SeatRangeRequest;
import com.seatwise.venue.dto.response.SeatCreateResponse;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class SeatServiceTest {

  @Autowired private SeatService seatService;
  @Autowired private VenueRepository venueRepository;
  @Autowired private SeatRepository seatRepository;

  private Venue venue;

  @BeforeEach
  void setUp() {
    venue = new Venue("에술의 전당", 200);
    venueRepository.save(venue);
  }

  @Test
  void shouldCreateSingleSeat_whenValidRequestProvided() {
    // given
    SeatRangeRequest seatRangeRequest = new SeatRangeRequest("A", 1, 1);
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(venue.getId(), List.of(seatRangeRequest));

    // when
    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);

    // then
    assertThat(seats.seatsId()).hasSize(1);
  }

  @Test
  void shouldCreateMultipleSeats_whenMultipleValidRangesProvided() {
    // given
    SeatRangeRequest aRowRequest = new SeatRangeRequest("A", 1, 10);
    SeatRangeRequest bRowRequest = new SeatRangeRequest("B", 1, 5);
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(venue.getId(), List.of(aRowRequest, bRowRequest));

    // when
    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);

    // then
    assertThat(seats.seatsId()).hasSize(15);
  }

  @Test
  void shouldThrowException_whenDuplicateSeatNumberExistsInVenue() {
    // given
    SeatRangeRequest seatRange = new SeatRangeRequest("A", 1, 5);
    SeatCreateRequest request = new SeatCreateRequest(venue.getId(), List.of(seatRange));
    Seat existingSeat = new Seat("A", "3", venue);
    seatRepository.save(existingSeat);

    // when & then
    assertThatThrownBy(() -> seatService.createSeat(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(BaseCode.DUPLICATE_SEAT_NUMBER.getMessage());
  }
}
