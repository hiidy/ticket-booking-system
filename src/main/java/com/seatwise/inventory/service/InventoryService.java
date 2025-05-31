package com.seatwise.inventory.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.inventory.dto.ShowInventoryResponse;
import com.seatwise.inventory.entity.ShowInventory;
import com.seatwise.inventory.repository.InventoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryRepository inventoryRepository;

  public List<ShowInventoryResponse> getShowInventory(Long showId) {
    return inventoryRepository.findByShowId(showId).stream()
        .map(
            inventory ->
                new ShowInventoryResponse(
                    inventory.getGrade().name(),
                    inventory.getTotalCount(),
                    inventory.getAvailableCount()))
        .toList();
  }

  @Transactional
  public void decreaseShowInventoryStock(Long id, int decreaseCount) {
    ShowInventory showInventory =
        inventoryRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NO_AVAILABLE_STOCK));

    showInventory.decreaseStock(decreaseCount);
  }
}
