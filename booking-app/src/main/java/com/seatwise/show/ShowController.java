package com.seatwise.show;

import com.seatwise.show.dto.request.ShowRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연 관리", description = "공연 생성 및 조회 관련 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ShowController {

  private final ShowService showService;

  @Operation(summary = "공연 생성", description = "새로운 공연 정보를 생성합니다")
  @PostMapping
  public ShowCreateResponse createEvent(@Valid @RequestBody ShowRequest showRequest) {
    return showService.createEvent(showRequest);
  }

  @Operation(summary = "공연 조회", description = "ID로 공연 정보를 조회합니다")
  @GetMapping("/{eventId}")
  public ShowResponse findEventById(@PathVariable Long eventId) {
    return showService.findEventById(eventId);
  }
}
