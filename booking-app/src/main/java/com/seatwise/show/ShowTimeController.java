package com.seatwise.show;

import com.seatwise.show.dto.request.ShowSearchCondition;
import com.seatwise.show.dto.request.ShowTimeCreateRequest;
import com.seatwise.show.dto.response.ShowSummaryResponse;
import com.seatwise.show.dto.response.ShowTimeCreateResponse;
import com.seatwise.show.dto.response.ShowTimeSummaryResponse;
import com.seatwise.show.service.ShowTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공연 시간 관리", description = "공연 시간 생성 및 조회 관련 API")
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {

  private final ShowTimeService showTimeService;

  @Operation(summary = "공연 시간 생성", description = "새로운 공연 시간 정보를 생성합니다")
  @PostMapping
  public ShowTimeCreateResponse createShowTime(
      @Valid @RequestBody ShowTimeCreateRequest createRequest) {
    return showTimeService.createShowTime(createRequest);
  }

  @Operation(summary = "공연 시간 검색", description = "조건에 맞는 공연 시간 목록을 검색합니다")
  @GetMapping
  public Slice<ShowSummaryResponse> searchShowTimes(
      @ModelAttribute ShowSearchCondition condition, Pageable pageable) {
    return showTimeService.searchShowTimes(condition, pageable);
  }

  @Operation(summary = "월별 공연 날짜 조회", description = "특정 공연의 특정 월별 예매 가능 날짜를 조회합니다")
  @GetMapping("/{showTimeId}/dates")
  public List<ShowTimeSummaryResponse> getShowDatesByMonth(
      @PathVariable Long showTimeId, @RequestParam int year, @RequestParam int month) {
    return showTimeService.getAvailableDates(showTimeId, year, month);
  }
}
