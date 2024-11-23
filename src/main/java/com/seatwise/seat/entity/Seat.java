package com.seatwise.seat.entity;

import com.seatwise.common.BaseEntity;
import com.seatwise.show.ShowSeat;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
  private SeatType type;

  @OneToMany(mappedBy = "seat")
  private List<ShowSeat> showSeats = new ArrayList<>();

  @Builder
  public Seat(int seatNumber, SeatType type) {
    this.seatNumber = seatNumber;
    this.type = type;
  }
}
