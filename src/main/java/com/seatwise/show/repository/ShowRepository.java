package com.seatwise.show.repository;

import com.seatwise.show.domain.Show;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

  List<Show> findByEventId(Long eventId);

  List<Show> findByEventIdAndDateBetween(Long eventId, LocalDate startDate, LocalDate endDate);

  List<Show> findShowsByEventIdAndDate(Long eventId, LocalDate date);
}
