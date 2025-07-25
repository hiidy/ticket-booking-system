package com.seatwise.venue.entity;

import com.seatwise.core.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Venue extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private int totalSeats;

  public Venue(String name, int totalSeats) {
    this.name = name;
    this.totalSeats = totalSeats;
  }
}
