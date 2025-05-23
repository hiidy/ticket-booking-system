package com.seatwise.show.domain;

import com.seatwise.common.domain.BaseEntity;
import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.event.domain.Event;
import com.seatwise.venue.domain.Venue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`show`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Show extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id")
  private Event event;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venue_id")
  private Venue venue;

  private LocalDate date;

  private LocalTime startTime;

  private LocalTime endTime;

  public Show(Event event, Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime) {
    validateTimes(startTime, endTime);
    this.event = event;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.venue = venue;
  }

  public void validateTimes(LocalTime startTime, LocalTime endTime) {
    if (startTime.isAfter(endTime)) {
      throw new BusinessException(ErrorCode.INVALID_SHOW_TIME);
    }
  }

  public boolean isOverlapping(Show other) {
    return isSameDate(other) && isTimeOverlapping(other);
  }

  private boolean isSameDate(Show other) {
    return this.date.equals(other.getDate());
  }

  private boolean isTimeOverlapping(Show other) {
    return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
  }
}
