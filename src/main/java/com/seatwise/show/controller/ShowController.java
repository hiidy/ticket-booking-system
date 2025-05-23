package com.seatwise.show.controller;

import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.request.ShowSearchCondition;
import com.seatwise.show.dto.request.ShowSeatCreateRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowDatesResponse;
import com.seatwise.show.dto.response.ShowSeatResponse;
import com.seatwise.show.dto.response.ShowSummaryResponse;
import com.seatwise.show.service.ShowSeatService;
import com.seatwise.show.service.ShowService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

  private final ShowService showService;
  private final ShowSeatService showSeatService;

  @PostMapping
  public ResponseEntity<Void> createShow(@Valid @RequestBody ShowCreateRequest createRequest) {
    ShowCreateResponse createResponse = showService.createShow(createRequest);
    return ResponseEntity.created(URI.create("/api/shows/" + createResponse.id())).build();
  }

  @GetMapping("/{eventId}/dates")
  public ResponseEntity<List<ShowDatesResponse>> getShowDatesByMonth(
      @PathVariable Long eventId, @RequestParam int year, @RequestParam int month) {
    List<ShowDatesResponse> dates = showService.getAvailableDates(eventId, year, month);
    return ResponseEntity.ok(dates);
  }

  @GetMapping("/{showId}/available-seats")
  public ResponseEntity<List<SeatAvailabilityResponse>> getAvailableSeatsForShow(
      @PathVariable Long showId) {
    List<SeatAvailabilityResponse> response = showSeatService.getAvailableSeatsForShow(showId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{showId}/seats")
  public ResponseEntity<List<ShowSeatResponse>> getShowSeats(@PathVariable Long showId) {
    List<ShowSeatResponse> response = showSeatService.getShowSeats(showId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{showId}/seats")
  public ResponseEntity<List<Long>> createShowSeat(
      @PathVariable Long showId, @Valid @RequestBody ShowSeatCreateRequest request) {
    List<Long> response = showSeatService.createShowSeat(showId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<ShowSummaryResponse>> getShows(
      @ModelAttribute ShowSearchCondition condition, Pageable pageable) {
    List<ShowSummaryResponse> responses = showService.getShows(condition, pageable);
    return ResponseEntity.ok(responses);
  }
}
