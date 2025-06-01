package com.seatwise.showtime;

import com.seatwise.showtime.dto.request.ShowSearchCondition;
import com.seatwise.showtime.dto.request.ShowSeatCreateRequest;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import com.seatwise.showtime.dto.response.ShowSeatResponse;
import com.seatwise.showtime.dto.response.ShowSummaryResponse;
import com.seatwise.showtime.dto.response.ShowTimeCreateResponse;
import com.seatwise.showtime.dto.response.ShowTimeSummaryResponse;
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
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {

  private final ShowTimeService showTimeService;
  private final ShowSeatService showSeatService;

  @PostMapping
  public ResponseEntity<Void> createShowTime(
      @Valid @RequestBody ShowTimeCreateRequest createRequest) {
    ShowTimeCreateResponse createResponse = showTimeService.createShow(createRequest);
    return ResponseEntity.created(URI.create("/api/showtimes/" + createResponse.id())).build();
  }

  @GetMapping("/{id}/dates")
  public ResponseEntity<List<ShowTimeSummaryResponse>> getShowDatesByMonth(
      @PathVariable("id") Long showTimeId, @RequestParam int year, @RequestParam int month) {
    List<ShowTimeSummaryResponse> availableDates =
        showTimeService.getAvailableDates(showTimeId, year, month);
    return ResponseEntity.ok(availableDates);
  }

  @GetMapping("/{id}/available-seats")
  public ResponseEntity<List<SeatAvailabilityResponse>> getAvailableSeatsForShow(
      @PathVariable Long id) {
    List<SeatAvailabilityResponse> response = showSeatService.getAvailableSeatsForShow(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/seats")
  public ResponseEntity<List<ShowSeatResponse>> getShowSeats(@PathVariable("id") Long showTimeId) {
    List<ShowSeatResponse> response = showSeatService.getShowSeats(showTimeId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{id}/seats")
  public ResponseEntity<List<Long>> createShowSeat(
      @PathVariable("id") Long showTimeId, @Valid @RequestBody ShowSeatCreateRequest request) {
    List<Long> response = showSeatService.createShowSeat(showTimeId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<ShowSummaryResponse>> searchShowTimes(
      @ModelAttribute ShowSearchCondition condition, Pageable pageable) {
    List<ShowSummaryResponse> responses = showTimeService.getShows(condition, pageable);
    return ResponseEntity.ok(responses);
  }
}
