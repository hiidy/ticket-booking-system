package com.seatwise.seat.controller;

import com.seatwise.seat.controller.request.SeatsCreateRequest;
import com.seatwise.seat.dto.SeatCreateRequest;
import com.seatwise.seat.dto.SeatCreateResponse;
import com.seatwise.seat.dto.SeatsCreateResponse;
import com.seatwise.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

  private final SeatService seatService;

  @PostMapping("/single")
  public SeatCreateResponse createSeat(@Valid @RequestBody SeatCreateRequest createRequest) {
    return seatService.createSeat(createRequest);
  }

  @PostMapping
  public SeatsCreateResponse createSeats(@RequestBody SeatsCreateRequest request) {
    return seatService.createSeats(request.toCreateDto());
  }
}
