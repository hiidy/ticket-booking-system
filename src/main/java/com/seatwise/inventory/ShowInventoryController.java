package com.seatwise.inventory;

import com.seatwise.inventory.dto.ShowInventoryResponse;
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
