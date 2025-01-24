package com.seatwise.show.domain;

import com.seatwise.booking.domain.Booking;
import com.seatwise.common.domain.BaseEntity;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
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
import jakarta.persistence.Version;
import lombok.AccessLevel;
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

  @Version private Long version;

  private ShowSeat(Show show, Seat seat, Integer price, Status status) {
    validatePrice(price);
    this.show = show;
    this.seat = seat;
    this.booking = null;
    this.price = price;
    this.status = status;
  }

  public static ShowSeat createAvailable(Show show, Seat seat, Integer price) {
    return new ShowSeat(show, seat, price, Status.AVAILABLE);
  }

  public void assignBooking(Booking booking) {
    validateBeforeAssignBooking();
    this.booking = booking;
    this.status = Status.RESERVED;
  }

  private void validateBeforeAssignBooking() {
    if (this.booking != null) {
      throw new BadRequestException(ErrorCode.SEAT_ALREADY_BOOKED);
    }
    if (this.status != Status.AVAILABLE) {
      throw new BadRequestException(ErrorCode.SEAT_NOT_AVAILABLE);
    }
  }

  private void validatePrice(Integer price) {
    if (price < 0) {
      throw new BadRequestException(ErrorCode.INVALID_SEAT_PRICE);
    }
  }
}
