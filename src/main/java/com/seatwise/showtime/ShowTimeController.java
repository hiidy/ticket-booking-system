package com.seatwise.showtime;

import com.seatwise.showtime.dto.request.ShowSearchCondition;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.dto.response.ShowSummaryResponse;
import com.seatwise.showtime.dto.response.ShowTimeCreateResponse;
import com.seatwise.showtime.dto.response.ShowTimeSummaryResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {

  private final ShowTimeService showTimeService;

  @PostMapping
  public ResponseEntity<Void> createShowTime(
      @Valid @RequestBody ShowTimeCreateRequest createRequest) {
    ShowTimeCreateResponse response = showTimeService.createShowTime(createRequest);
    return ResponseEntity.created(URI.create("/api/showtimes/" + response.id())).build();
  }

  @GetMapping
  public ResponseEntity<List<ShowSummaryResponse>> searchShowTimes(
      @ModelAttribute ShowSearchCondition condition, Pageable pageable) {
    List<ShowSummaryResponse> responses = showTimeService.searchShowTimes(condition, pageable);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{showTimeId}/dates")
  public ResponseEntity<List<ShowTimeSummaryResponse>> getShowDatesByMonth(
      @PathVariable Long showTimeId, @RequestParam int year, @RequestParam int month) {
    List<ShowTimeSummaryResponse> dates =
        showTimeService.getAvailableDates(showTimeId, year, month);
    return ResponseEntity.ok(dates);
  }
}
