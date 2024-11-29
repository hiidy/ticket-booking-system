package com.seatwise.seat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.seatwise.seat.dto.SeatsCreateRequest;
import com.seatwise.seat.dto.SeatsCreateResponse;
import com.seatwise.seat.entity.Seat;
import com.seatwise.seat.entity.SeatType;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.seat.service.SeatService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

  @InjectMocks private SeatService seatService;
  @Mock private SeatRepository seatRepository;

  @Test
  @DisplayName("여러개의 좌석 생성")
  public void testCreateMultipleSeat() {
    SeatsCreateRequest request = new SeatsCreateRequest(1L, 3, "A");
    List<Seat> expectedSeats =
        List.of(
            Seat.builder().seatNumber(1).type(SeatType.A).build(),
            Seat.builder().seatNumber(2).type(SeatType.A).build(),
            Seat.builder().seatNumber(3).type(SeatType.A).build());
    when(seatRepository.saveAll(anyList())).thenReturn(expectedSeats);

    SeatsCreateResponse response = seatService.createSeats(request);

    assertThat(response.seatsId().size()).isEqualTo(3);
  }
}
