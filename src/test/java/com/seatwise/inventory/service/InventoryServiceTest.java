package com.seatwise.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.inventory.domain.ShowInventory;
import com.seatwise.inventory.domain.ShowInventoryPk;
import com.seatwise.inventory.repository.InventoryRepository;
import com.seatwise.seat.domain.SeatGrade;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock private InventoryRepository inventoryRepository;
  @InjectMocks private InventoryService inventoryService;

  @Test
  void givenShowInventory_whenDecreaseShowInventoryStock_thenAvailableStockIsDecreased() {
    // given
    ShowInventoryPk pk = new ShowInventoryPk(1L, SeatGrade.VIP);
    ShowInventory vipInventory = new ShowInventory(pk, 100, 100);
    int decreaseCount = 10;

    when(inventoryRepository.findById(pk)).thenReturn(Optional.of(vipInventory));
    // when
    inventoryService.decreaseShowInventoryStock(pk, decreaseCount);

    // then
    verify(inventoryRepository).findById(pk);
    assertThat(vipInventory.getAvailableCount())
        .isEqualTo(vipInventory.getTotalCount() - decreaseCount);
  }

  @Test
  void givenNotExistsShowInventoryPk_whenDecreaseShowInventoryStock_thenThrowsException() {
    // given
    ShowInventoryPk pk = new ShowInventoryPk(1L, SeatGrade.VIP);
    int decreaseCount = 10;

    when(inventoryRepository.findById(pk)).thenReturn(Optional.empty());
    // when & then
    assertThatThrownBy(() -> inventoryService.decreaseShowInventoryStock(pk, decreaseCount))
        .isInstanceOf(BusinessException.class);
    verify(inventoryRepository).findById(pk);
  }
}
