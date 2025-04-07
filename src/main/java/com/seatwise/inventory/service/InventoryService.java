package com.seatwise.inventory.service;

import com.seatwise.inventory.dto.ShowInventoryResponse;
import com.seatwise.inventory.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryRepository inventoryRepository;

  public List<ShowInventoryResponse> getShowInventory(Long showId) {

    return inventoryRepository.findById_ShowId(showId).stream()
        .map(
            inventory ->
                new ShowInventoryResponse(
                    inventory.getId().getGrade().name(),
                    inventory.getTotalCount(),
                    inventory.getAvailableCount()))
        .toList();
  }
}
