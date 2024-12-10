package com.seatwise.show.controller;

import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.dto.response.ShowSeatCreateResponse;
import com.seatwise.show.service.ShowSeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/showseats")
@RequiredArgsConstructor
public class ShowSeatController {

  private final ShowSeatService showSeatService;

  @PostMapping
  public ResponseEntity<ShowSeatCreateResponse> createShowSeat(
      @Valid @RequestBody ShowSeatCreateRequest createRequest) {
    ShowSeatCreateResponse response = showSeatService.createShowSeat(createRequest.toCreateDto());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
