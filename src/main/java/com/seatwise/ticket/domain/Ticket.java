package com.seatwise.ticket.domain;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.core.jpa.BaseEntity;
import com.seatwise.showtime.domain.ShowTime;
import com.seatwise.venue.domain.Seat;
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
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "show_time_id")
  private ShowTime showTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seat_id")
  private Seat seat;

  private Long bookingId;

  private Integer price;

  @Enumerated(EnumType.STRING)
  private Status status;

  private LocalDateTime expirationTime;

  private Ticket(ShowTime showTime, Seat seat, Integer price, Status status) {
    validatePrice(price);
    this.showTime = showTime;
    this.seat = seat;
    this.bookingId = null;
    this.price = price;
    this.status = status;
  }

  public static Ticket createAvailable(ShowTime showTime, Seat seat, Integer price) {
    return new Ticket(showTime, seat, price, Status.AVAILABLE);
  }

  public boolean canAssignBooking(LocalDateTime now) {
    if (status == Status.AVAILABLE) {
      return expirationTime == null;
    }
    if (status == Status.PAYMENT_PENDING) {
      return expirationTime != null && expirationTime.isBefore(now);
    }
    return false;
  }

  public void assignBooking(Long bookingId, LocalDateTime requestTime, Duration duration) {
    this.bookingId = bookingId;
    this.status = Status.PAYMENT_PENDING;
    this.expirationTime = requestTime.plus(duration);
  }

  private void validatePrice(Integer price) {
    if (price < 0) {
      throw new BusinessException(ErrorCode.INVALID_SEAT_PRICE);
    }
  }
}
