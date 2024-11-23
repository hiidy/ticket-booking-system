package com.seatwise.seat.controller;

import com.seatwise.seat.dto.SeatCreateRequest;
import com.seatwise.seat.dto.SeatCreateResponse;
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

  @PostMapping
  public SeatCreateResponse createSeat(@Valid @RequestBody SeatCreateRequest createRequest) {
    return seatService.createSeat(createRequest);
  }
}
