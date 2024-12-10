package com.seatwise.show.domain;

import com.seatwise.common.BaseEntity;
import com.seatwise.event.domain.Event;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
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

  private LocalDate date;

  private LocalTime startTime;

  private LocalTime endTime;

  @OneToMany(mappedBy = "show")
  private List<ShowSeat> showSeats = new ArrayList<>();

  @Builder
  public Show(Event event, LocalDate date, LocalTime startTime, LocalTime endTime) {
    this.event = event;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
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
