package com.seatwise.show.entity;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.core.jpa.BaseEntity;
import com.seatwise.venue.entity.Venue;
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
@Table(name = "`show_time`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowTime extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "show_id")
  private Show show;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venue_id")
  private Venue venue;

  private LocalDate date;

  private LocalTime startTime;

  private LocalTime endTime;

  public ShowTime(Show show, Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime) {
    validateTimes(startTime, endTime);
    this.show = show;
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

  public boolean isOverlapping(ShowTime other) {
    return isSameDate(other) && isTimeOverlapping(other);
  }

  private boolean isSameDate(ShowTime other) {
    return this.date.equals(other.getDate());
  }

  private boolean isTimeOverlapping(ShowTime other) {
    return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
  }
}
