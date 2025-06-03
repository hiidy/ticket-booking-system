package com.seatwise.inventory;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.inventory.domain.ShowInventory;
import com.seatwise.inventory.domain.ShowInventoryRepository;
import com.seatwise.inventory.dto.ShowInventoryResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowInventoryService {

  private final ShowInventoryRepository showInventoryRepository;

  public List<ShowInventoryResponse> getShowInventory(Long showId) {
    return showInventoryRepository.findByShowId(showId).stream()
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
        showInventoryRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.NO_AVAILABLE_STOCK));

    showInventory.decreaseStock(decreaseCount);
  }
}
