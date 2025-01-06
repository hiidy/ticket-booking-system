package com.seatwise.show.domain;

import com.seatwise.booking.domain.Booking;
import com.seatwise.common.domain.BaseEntity;
import com.seatwise.seat.domain.Seat;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "show_seat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSeat extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "show_id")
  private Show show;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seat_id")
  private Seat seat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id")
  private Booking booking;

  private Integer price;

  @Enumerated(EnumType.STRING)
  private Status status;

  @Builder
  public ShowSeat(Show show, Seat seat, Integer price, Status status) {
    this.show = show;
    this.seat = seat;
    this.price = price;
    this.status = status;
  }

  public void assignBooking(Booking booking) {
    this.booking = booking;
    this.status = Status.RESERVED;
  }
}
