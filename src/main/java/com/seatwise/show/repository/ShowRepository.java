package com.seatwise.show.repository;

import com.seatwise.event.domain.EventType;
import com.seatwise.show.domain.Show;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

  List<Show> findByEventId(Long eventId);

  List<Show> findByEventIdAndDateBetween(Long eventId, LocalDate startDate, LocalDate endDate);

  List<Show> findShowsByEventIdAndDate(Long eventId, LocalDate date);

  @EntityGraph(attributePaths = {"event", "venue"})
  Slice<Show> findByEvent_TypeAndDateAfterOrderByDateAsc(
      EventType type, LocalDate date, Pageable pageable);
}
