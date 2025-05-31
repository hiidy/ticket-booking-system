package com.seatwise.inventory.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.seat.entity.SeatGrade;
import org.junit.jupiter.api.Test;

class ShowTimeInventoryTest {

  @Test
  void givenShowInventory_whenDecreaseStockOnce_thenStockDecreasedByOne() {
    // given
    int totalCount = 100;
    int decreaseCount = 1;
    ShowInventory showInventory = new ShowInventory(1L, SeatGrade.S, totalCount, totalCount);

    // when
    showInventory.decreaseStock(decreaseCount);

    // then
    assertThat(showInventory.getAvailableCount()).isEqualTo(totalCount - decreaseCount);
  }

  @Test
  void givenShowInventoryWithNoAvailableCount_whenDecreaseStock_thenThrowsException() {
    // given
    int totalCount = 100;
    int availableCount = 0;
    int decreaseCount = 1;
    ShowInventory showInventory = new ShowInventory(1L, SeatGrade.S, totalCount, availableCount);

    // when & then
    assertThatThrownBy(() -> showInventory.decreaseStock(decreaseCount))
        .isInstanceOf(BusinessException.class);
  }
}
