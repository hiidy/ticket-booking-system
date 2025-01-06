package com.seatwise.seat;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatwise.seat.dto.request.SeatCreateRequest;
import com.seatwise.seat.dto.request.SeatTypeRangeRequest;
import com.seatwise.seat.dto.response.SeatCreateResponse;
import com.seatwise.seat.service.SeatService;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class SeatServiceTest {

  @Autowired private SeatService seatService;

  @Autowired private VenueRepository venueRepository;

  @BeforeEach
  void setUp() {
    Venue venue = Venue.builder().name("예술의 전당").totalSeats(200).build();
    venueRepository.save(venue);
  }

  @Test
  @DisplayName("단일 좌석 생성 테스트")
  public void createSeat_WithValidRequest_Success() {
    SeatTypeRangeRequest seatTypeRangeRequest = new SeatTypeRangeRequest(1, 1, "S");
    SeatCreateRequest seatCreateRequest = new SeatCreateRequest(1L, List.of(seatTypeRangeRequest));

    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);
    assertThat(seats.seatsId()).hasSize(1);
  }

  @Test
  @DisplayName("다중 좌석 생성 테스트")
  public void createSeats_WithValidRequest_Success() {
    SeatTypeRangeRequest sSeatRequest = new SeatTypeRangeRequest(1, 10, "S");
    SeatTypeRangeRequest vipSeatRequest = new SeatTypeRangeRequest(11, 15, "VIP");
    SeatCreateRequest seatCreateRequest =
        new SeatCreateRequest(1L, List.of(sSeatRequest, vipSeatRequest));

    SeatCreateResponse seats = seatService.createSeat(seatCreateRequest);
    assertThat(seats.seatsId()).hasSize(15);
  }
}
