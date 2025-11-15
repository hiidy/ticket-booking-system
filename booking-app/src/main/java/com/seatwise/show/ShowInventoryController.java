package com.seatwise.show;

import com.seatwise.show.dto.response.ShowInventoryResponse;
import com.seatwise.show.service.ShowInventoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows/{showId}/inventory")
@RequiredArgsConstructor
public class ShowInventoryController {

  private final ShowInventoryService showInventoryService;

  @GetMapping
  public ResponseEntity<List<ShowInventoryResponse>> getShowInventory(@PathVariable Long showId) {
    return ResponseEntity.ok(showInventoryService.getShowInventory(showId));
  }
}