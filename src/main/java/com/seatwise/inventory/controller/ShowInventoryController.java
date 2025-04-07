package com.seatwise.inventory.controller;

import com.seatwise.inventory.dto.ShowInventoryResponse;
import com.seatwise.inventory.service.InventoryService;
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

  private final InventoryService inventoryService;

  @GetMapping
  public ResponseEntity<List<ShowInventoryResponse>> getShowInventory(@PathVariable Long showId) {
    return ResponseEntity.ok(inventoryService.getShowInventory(showId));
  }
}
