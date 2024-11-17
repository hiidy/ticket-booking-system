package com.seatwise.show;

import com.seatwise.common.BaseEntity;
import com.seatwise.event.entity.Event;
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
}
