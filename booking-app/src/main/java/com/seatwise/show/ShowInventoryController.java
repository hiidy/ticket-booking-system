package com.seatwise.show;

import com.seatwise.show.dto.response.ShowInventoryResponse;
import com.seatwise.show.service.ShowInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연 재고 관리", description = "공연 좌석 재고 관련 API")
@RestController
@RequestMapping("/api/shows/{showId}/inventory")
@RequiredArgsConstructor
public class ShowInventoryController {

  private final ShowInventoryService showInventoryService;

  @Operation(summary = "공연 재고 조회", description = "특정 공연의 전체 좌석 재고 정보를 조회합니다")
  @GetMapping
  public List<ShowInventoryResponse> getShowInventory(@PathVariable Long showId) {
    return showInventoryService.getShowInventory(showId);
  }
}
