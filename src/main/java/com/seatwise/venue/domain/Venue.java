package com.seatwise.venue.domain;

import com.seatwise.common.domain.BaseEntity;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.domain.Seat;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  @OneToMany(
      mappedBy = "venue",
      cascade = {CascadeType.REMOVE},
      orphanRemoval = true)
  private List<Seat> seats = new ArrayList<>();

  public Venue(String name, int totalSeats) {
    this.name = name;
    this.totalSeats = totalSeats;
  }

  public void validateNewSeatNumbers(List<Integer> seatNumbers) {
    Set<Integer> existingNumbers =
        seats.stream().map(Seat::getSeatNumber).collect(Collectors.toSet());

    boolean hasDuplicate = seatNumbers.stream().anyMatch(existingNumbers::contains);

    if (hasDuplicate) {
      throw new BadRequestException(ErrorCode.DUPLICATE_SEAT_NUMBER);
    }
  }

  public void addSeat(Seat seat) {
    seats.add(seat);
  }
}
