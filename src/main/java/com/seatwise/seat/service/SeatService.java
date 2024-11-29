package com.seatwise.seat.service;

import com.seatwise.seat.dto.SeatCreateRequest;
import com.seatwise.seat.dto.SeatCreateResponse;
import com.seatwise.seat.dto.SeatsCreateRequest;
import com.seatwise.seat.dto.SeatsCreateResponse;
import com.seatwise.seat.entity.Seat;
import com.seatwise.seat.entity.SeatType;
import com.seatwise.seat.repository.SeatRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatService {

  private final SeatRepository seatRepository;

  public SeatCreateResponse createSeat(SeatCreateRequest seatCreateRequest) {
    Seat saveSeat = seatRepository.save(seatCreateRequest.toEntity());
    return SeatCreateResponse.from(saveSeat);
  }

  public SeatsCreateResponse createSeats(SeatsCreateRequest createRequest) {
    List<Seat> seats = new ArrayList<>();
    for (int i = 1; i <= createRequest.maxSeatNumber(); i++) {
      seats.add(
          Seat.builder().seatNumber(i).type(SeatType.valueOf(createRequest.seatType())).build());
    }
    List<Seat> savedSeats = seatRepository.saveAll(seats);
    return SeatsCreateResponse.from(savedSeats);
  }
}
