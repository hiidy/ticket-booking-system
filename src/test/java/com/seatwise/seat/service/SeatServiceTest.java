package com.seatwise.seat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.annotation.ServiceTest;
import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.domain.SeatGrade;
import com.seatwise.seat.dto.request.SeatCreateRequest;
import com.seatwise.seat.dto.request.SeatGradeRangeRequest;
import com.seatwise.seat.dto.response.SeatCreateResponse;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("단일 좌석 생성 테스트")
  void createSeat_WithValidRequest_Success() {

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
  @DisplayName("다중 좌석 생성 테스트")
  void createSeats_WithValidRequest_Success() {
    // Given
    SeatGradeRangeRequest sSeatRequest = new SeatGradeRangeRequest(1, 10, "S");
    SeatGradeRangeRequest vipSeatRequest = new SeatGradeRangeRequest(11, 15, "VIP");
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(venue.getId(), List.of(sSeatRequest, vipSeatRequest));

    // When
    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);

    // Then
    assertThat(seats.seatsId()).hasSize(15);
  }

  @Test
  @DisplayName("기존 venue에 중복된 좌석 번호를 생성하면 예외 발생")
  void createSeat_WithDuplicateSeatNumber_ThrowsException() {
    // given
    SeatGradeRangeRequest seatRange = new SeatGradeRangeRequest(1, 20, "A");
    SeatCreateRequest request = new SeatCreateRequest(venue.getId(), List.of(seatRange));
    Seat seat = Seat.builder().venue(venue).seatNumber(1).grade(SeatGrade.S).build();
    seatRepository.save(seat);

    // when & then
    assertThatThrownBy(() -> seatService.createSeat(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.DUPLICATE_SEAT_NUMBER.getMessage());
  }
}
