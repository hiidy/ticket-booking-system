package com.seatwise.show;

import com.seatwise.show.dto.request.TicketCreateRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.TicketResponse;
import com.seatwise.show.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "티켓 관리", description = "티켓 생성 및 조회 관련 API")
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @Operation(summary = "티켓 생성", description = "특정 공연 시간의 티켓들을 생성합니다")
  @PostMapping("/{showTimeId}/seats")
  public List<Long> createTickets(
      @PathVariable Long showTimeId, @Valid @RequestBody TicketCreateRequest request) {
    return ticketService.createTickets(showTimeId, request);
  }

  @Operation(summary = "티켓 목록 조회", description = "특정 공연 시간의 티켓 목록을 조회합니다")
  @GetMapping("/{showTimeId}/seats")
  public List<TicketResponse> getTickets(@PathVariable Long showTimeId) {
    return ticketService.getTickets(showTimeId);
  }

  @Operation(summary = "좌석 가용성 조회", description = "공연 시간별 등급별 좌석 가용성을 조회합니다")
  @GetMapping("/availability")
  public List<SeatAvailabilityResponse> getTicketAvailabilityByGrade(
      @RequestParam Long showTimeId) {
    return ticketService.getTicketAvailabilityByGrade(showTimeId);
  }
}
