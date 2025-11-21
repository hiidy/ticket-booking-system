package com.seatwise.show.entity;

import com.seatwise.core.BaseEntity;
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
@Table(name = "section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long showId;

  private String sectionName;

  @Enumerated(EnumType.STRING)
  private SectionType sectionType;

  private Integer basePrice;

  private Integer totalSeats;

  private Integer availableSeats;

  public Section(
      Long showId,
      String sectionName,
      SectionType sectionType,
      Integer basePrice,
      Integer totalSeats) {
    this.showId = showId;
    this.sectionName = sectionName;
    this.sectionType = sectionType;
    this.basePrice = basePrice;
    this.totalSeats = totalSeats;
    this.availableSeats = totalSeats;
  }

  public boolean hasAvailableSeats(int requestedCount) {
    return this.availableSeats >= requestedCount;
  }
}
