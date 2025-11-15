package com.seatwise.show;

import com.seatwise.show.dto.request.ShowSearchCondition;
import com.seatwise.show.dto.request.ShowTimeCreateRequest;
import com.seatwise.show.dto.response.ShowSummaryResponse;
import com.seatwise.show.dto.response.ShowTimeCreateResponse;
import com.seatwise.show.dto.response.ShowTimeSummaryResponse;
import com.seatwise.show.service.ShowTimeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
  public ResponseEntity<Slice<ShowSummaryResponse>> searchShowTimes(
      @ModelAttribute ShowSearchCondition condition, Pageable pageable) {
    Slice<ShowSummaryResponse> responses = showTimeService.searchShowTimes(condition, pageable);
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
