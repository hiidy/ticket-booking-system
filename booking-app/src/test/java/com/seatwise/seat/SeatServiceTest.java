package com.seatwise.seat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.venue.SeatService;
import com.seatwise.venue.entity.Seat;
import com.seatwise.venue.entity.SeatGrade;
import com.seatwise.venue.entity.SeatRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import com.seatwise.venue.dto.request.SeatCreateRequest;
import com.seatwise.venue.dto.request.SeatGradeRangeRequest;
import com.seatwise.venue.dto.response.SeatCreateResponse;
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
    SeatGradeRangeRequest seatGradeRangeRequest = new SeatGradeRangeRequest(1, 1, "S");
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(venue.getId(), List.of(seatGradeRangeRequest));

    // when
    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);

    // then
    assertThat(seats.seatsId()).hasSize(1);
  }

  @Test
  void shouldCreateMultipleSeats_whenMultipleValidRangesProvided() {
    // given
    SeatGradeRangeRequest sSeatRequest = new SeatGradeRangeRequest(1, 10, "S");
    SeatGradeRangeRequest vipSeatRequest = new SeatGradeRangeRequest(11, 15, "VIP");
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(venue.getId(), List.of(sSeatRequest, vipSeatRequest));

    // when
    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);

    // then
    assertThat(seats.seatsId()).hasSize(15);
  }

  @Test
  void shouldThrowException_whenDuplicateSeatNumberExistsInVenue() {
    // given
    SeatGradeRangeRequest seatRange = new SeatGradeRangeRequest(1, 20, "A");
    SeatCreateRequest request = new SeatCreateRequest(venue.getId(), List.of(seatRange));
    Seat existingSeat = Seat.builder().venue(venue).seatNumber(1).grade(SeatGrade.S).build();
    seatRepository.save(existingSeat);

    // when & then
    assertThatThrownBy(() -> seatService.createSeat(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.DUPLICATE_SEAT_NUMBER.getMessage());
  }
}
