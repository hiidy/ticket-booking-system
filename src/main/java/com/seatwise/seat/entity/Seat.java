package com.seatwise.seat.entity;

import com.seatwise.common.BaseEntity;
import com.seatwise.show.ShowSeat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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

  private int seatNumber;

  private SeatType seatType;

  @OneToMany(mappedBy = "seat")
  private List<ShowSeat> showSeats = new ArrayList<>();
}
