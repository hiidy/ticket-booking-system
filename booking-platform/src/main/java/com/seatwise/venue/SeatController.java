package com.seatwise.venue;

import com.seatwise.venue.dto.request.SeatCreateRequest;
import com.seatwise.venue.dto.response.SeatCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<SeatCreateResponse> createSeat(@RequestBody SeatCreateRequest request) {
    SeatCreateResponse response = seatService.createSeat(request);
    return ResponseEntity.ok(response);
  }
}
