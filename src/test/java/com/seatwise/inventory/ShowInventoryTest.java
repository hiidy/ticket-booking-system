package com.seatwise.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.inventory.domain.ShowInventory;
import com.seatwise.inventory.domain.ShowInventoryPk;
import com.seatwise.seat.domain.SeatGrade;
import org.junit.jupiter.api.Test;

class ShowInventoryTest {

  @Test
  void givenShowInventory_whenDecreaseStockOnce_thenStockDecreasedByOne() {
    // given
    int totalCount = 100;
    int decreaseCount = 1;
    ShowInventoryPk pk = new ShowInventoryPk(1L, SeatGrade.S);
    ShowInventory showInventory = new ShowInventory(pk, totalCount, totalCount);

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
    ShowInventoryPk pk = new ShowInventoryPk(1L, SeatGrade.S);
    ShowInventory showInventory = new ShowInventory(pk, totalCount, availableCount);

    // when & then
    assertThatThrownBy(() -> showInventory.decreaseStock(decreaseCount))
        .isInstanceOf(BadRequestException.class);
  }
}
