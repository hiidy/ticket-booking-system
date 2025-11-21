package com.seatwise.show.entity;

import com.seatwise.core.BaseCode;
import com.seatwise.core.BaseEntity;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.venue.entity.Venue;
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
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`show`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Show extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  private String description;

  @Enumerated(EnumType.STRING)
  private ShowType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venue_id")
  private Venue venue;

  private LocalDate date;

  private LocalTime startTime;

  private LocalTime endTime;

  public Show(
      String title,
      String description,
      ShowType type,
      Venue venue,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime) {
    this.title = title;
    this.description = description;
    this.type = type;
    this.venue = venue;
    this.date = date;
    setTimes(startTime, endTime);
  }

  public void setTimes(LocalTime startTime, LocalTime endTime) {
    validateTimes(startTime, endTime);
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public void validateTimes(LocalTime startTime, LocalTime endTime) {
    if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
      throw new BusinessException(BaseCode.INVALID_SHOW_TIME);
    }
  }

  public boolean isOverlapping(Show other) {
    if (other == null || this.date == null || other.getDate() == null) {
      return false;
    }
    return isSameDate(other) && isTimeOverlapping(other);
  }

  private boolean isSameDate(Show other) {
    return this.date.equals(other.getDate());
  }

  private boolean isTimeOverlapping(Show other) {
    if (this.startTime == null
        || this.endTime == null
        || other.getStartTime() == null
        || other.getEndTime() == null) {
      return false;
    }
    return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
  }
}
