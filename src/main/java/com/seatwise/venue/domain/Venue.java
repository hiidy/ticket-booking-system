package com.seatwise.venue.domain;

import com.seatwise.common.domain.BaseEntity;
import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.dto.request.SeatTypeRangeRequest;
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
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Builder;
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

  @OneToMany(mappedBy = "venue")
  private List<Seat> seats = new ArrayList<>();

  @Builder
  public Venue(String name, int totalSeats) {
    this.name = name;
    this.totalSeats = totalSeats;
  }

  public void validateNewSeatNumbers(List<SeatTypeRangeRequest> ranges) {
    Set<Integer> existingNumbers =
        seats.stream().map(Seat::getSeatNumber).collect(Collectors.toSet());

    boolean hasDuplicate =
        ranges.stream()
            .flatMap(range -> IntStream.rangeClosed(range.startNumber(), range.endNumber()).boxed())
            .anyMatch(existingNumbers::contains);

    if (hasDuplicate) {
      throw new ConflictException(ErrorCode.DUPLICATE_SEAT_NUMBER);
    }
  }
}
