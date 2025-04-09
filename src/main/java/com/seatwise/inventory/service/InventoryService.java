package com.seatwise.inventory.service;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.inventory.domain.ShowInventory;
import com.seatwise.inventory.domain.ShowInventoryPk;
import com.seatwise.inventory.dto.ShowInventoryResponse;
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
    return inventoryRepository.findById_ShowId(showId).stream()
        .map(
            inventory ->
                new ShowInventoryResponse(
                    inventory.getId().getGrade().name(),
                    inventory.getTotalCount(),
                    inventory.getAvailableCount()))
        .toList();
  }

  @Transactional
  public void decreaseShowInventoryStock(ShowInventoryPk pk, int decreaseCount) {
    ShowInventory showInventory =
        inventoryRepository
            .findById(pk)
            .orElseThrow(() -> new BadRequestException(ErrorCode.NO_AVAILABLE_STOCK));

    showInventory.decreaseStock(decreaseCount);
  }
}
