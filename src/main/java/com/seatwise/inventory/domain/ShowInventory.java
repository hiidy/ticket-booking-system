package com.seatwise.inventory.domain;

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
}
