package com.seatwise.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.seatwise.core.BusinessException;
import com.seatwise.show.entity.ShowInventory;
import com.seatwise.show.repository.ShowInventoryRepository;
import com.seatwise.show.service.ShowInventoryService;
import com.seatwise.venue.entity.SeatGrade;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowInventoryServiceTest {

  @Mock private ShowInventoryRepository showInventoryRepository;
  @InjectMocks private ShowInventoryService showInventoryService;

  @Test
  void shouldDecreaseAvailableCount_whenInventoryExists() {
    // given
    ShowInventory vipInventory = new ShowInventory(1L, SeatGrade.VIP, 100, 100);
    int decreaseCount = 10;

    when(showInventoryRepository.findById(1L)).thenReturn(Optional.of(vipInventory));

    // when
    showInventoryService.decreaseShowInventoryStock(1L, decreaseCount);

    // then
    verify(showInventoryRepository).findById(1L);
    assertThat(vipInventory.getAvailableCount())
        .isEqualTo(vipInventory.getTotalCount() - decreaseCount);
  }

  @Test
  void shouldThrowException_whenInventoryDoesNotExist() {
    // given
    Long inventoryId = 1L;
    int decreaseCount = 10;

    when(showInventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> showInventoryService.decreaseShowInventoryStock(inventoryId, decreaseCount))
        .isInstanceOf(BusinessException.class);

    verify(showInventoryRepository).findById(inventoryId);
  }
}
