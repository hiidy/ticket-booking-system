package com.seatwise.inventory.domain;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowInventory {

  @EmbeddedId private ShowInventoryPk id;

  private int totalCount;

  private int availableCount;

  public ShowInventory(ShowInventoryPk id, int totalCount, int availableCount) {
    this.id = id;
    this.totalCount = totalCount;
    this.availableCount = availableCount;
  }

  public void decreaseStock(int count) {
    if (availableCount <= 0) {
      throw new BusinessException(ErrorCode.NO_AVAILABLE_STOCK);
    }
    this.availableCount -= count;
  }
}
