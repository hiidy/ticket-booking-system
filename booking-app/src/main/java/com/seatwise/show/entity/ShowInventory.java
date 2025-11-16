package com.seatwise.show.entity;

import com.seatwise.core.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.venue.entity.SeatGrade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowInventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long showId;

  @Enumerated(EnumType.STRING)
  private SeatGrade grade;

  private int totalCount;

  private int availableCount;

  public ShowInventory(Long showId, SeatGrade grade, int totalCount, int availableCount) {
    this.showId = showId;
    this.grade = grade;
    this.totalCount = totalCount;
    this.availableCount = availableCount;
  }

  public void decreaseStock(int count) {
    if (availableCount <= 0) {
      throw new BusinessException(BaseCode.NO_AVAILABLE_STOCK);
    }
    this.availableCount -= count;
  }
}
