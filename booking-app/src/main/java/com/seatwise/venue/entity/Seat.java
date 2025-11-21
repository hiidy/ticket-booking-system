package com.seatwise.venue.entity;

import com.seatwise.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

  private String rowName;

  private String colName;

  @ManyToOne(fetch = FetchType.LAZY)
  private Venue venue;

  public Seat(String rowName, String colName, Venue venue) {
    this.rowName = rowName;
    this.colName = colName;
    this.venue = venue;
  }
}
