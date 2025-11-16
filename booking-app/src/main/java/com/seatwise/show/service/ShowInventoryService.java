package com.seatwise.show.service;

import com.seatwise.core.exception.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.dto.response.ShowInventoryResponse;
import com.seatwise.show.entity.ShowInventory;
import com.seatwise.show.repository.ShowInventoryRepository;
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
            .orElseThrow(() -> new BusinessException(BaseCode.NO_AVAILABLE_STOCK));

    showInventory.decreaseStock(decreaseCount);
  }
}
