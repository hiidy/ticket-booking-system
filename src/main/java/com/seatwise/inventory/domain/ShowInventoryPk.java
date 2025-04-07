package com.seatwise.inventory.domain;

import com.seatwise.seat.domain.SeatGrade;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ShowInventoryPk implements Serializable {

  private Long showId;

  @Enumerated(EnumType.STRING)
  private SeatGrade grade;

  public ShowInventoryPk(Long showId, SeatGrade grade) {
    this.showId = showId;
    this.grade = grade;
  }
}
