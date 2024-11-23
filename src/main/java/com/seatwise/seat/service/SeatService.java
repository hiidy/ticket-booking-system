package com.seatwise.seat.service;

import com.seatwise.seat.dto.SeatCreateRequest;
import com.seatwise.seat.dto.SeatCreateResponse;
import com.seatwise.seat.entity.Seat;
import com.seatwise.seat.repository.SeatRepository;
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
}
