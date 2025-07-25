package com.seatwise.venue.entity;

import com.seatwise.core.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private int seatNumber;

  @Enumerated(EnumType.STRING)
  private SeatGrade grade;

  @ManyToOne(fetch = FetchType.LAZY)
  private Venue venue;

  @Builder
  public Seat(int seatNumber, SeatGrade grade, Venue venue) {
    this.seatNumber = seatNumber;
    this.grade = grade;
    this.venue = venue;
  }
}
