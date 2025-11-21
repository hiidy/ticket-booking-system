package com.seatwise.show.entity;

import com.seatwise.core.BaseCode;
import com.seatwise.core.BaseEntity;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.venue.entity.Seat;
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
  @JoinColumn(name = "show_id")
  private Show show;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seat_id")
  private Seat seat;

  private Long sectionId;

  private Long bookingId;

  private Integer price;

  @Enumerated(EnumType.STRING)
  private TicketStatus status;

  private LocalDateTime expirationTime;

  private Ticket(Show show, Seat seat, Long sectionId, Integer price, TicketStatus status) {
    validatePrice(price);
    this.show = show;
    this.seat = seat;
    this.sectionId = sectionId;
    this.bookingId = null;
    this.price = price;
    this.status = status;
  }

  public static Ticket createAvailable(Show show, Seat seat, Long sectionId, Integer price) {
    return new Ticket(show, seat, sectionId, price, TicketStatus.AVAILABLE);
  }

  public boolean canAssignBooking(LocalDateTime now) {
    if (status == TicketStatus.AVAILABLE) {
      return expirationTime == null;
    }
    if (status == TicketStatus.PAYMENT_PENDING) {
      return expirationTime != null && expirationTime.isBefore(now);
    }
    return false;
  }

  public void assignBooking(Long bookingId, LocalDateTime requestTime, Duration duration) {
    this.bookingId = bookingId;
    this.status = TicketStatus.PAYMENT_PENDING;
    this.expirationTime = requestTime.plus(duration);
  }

  public void cancelBooking() {
    this.bookingId = null;
    this.status = TicketStatus.CANCELLED;
    this.expirationTime = null;
  }

  private void validatePrice(Integer price) {
    if (price < 0) {
      throw new BusinessException(BaseCode.INVALID_SEAT_PRICE);
    }
  }
}
