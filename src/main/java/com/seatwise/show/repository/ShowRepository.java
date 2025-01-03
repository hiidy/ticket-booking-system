package com.seatwise.show.repository;

import com.seatwise.show.domain.Show;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

  List<Show> findByEventId(Long eventId);

  @Query(
      "SELECT s.date FROM Show s WHERE s.event.id = :eventId AND s.date BETWEEN :startDate AND :endDate")
  List<LocalDate> findShowDatesByEventIdAndDateBetween(
      @Param("eventId") Long eventId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  List<Show> findShowsByEventIdAndDate(Long eventId, LocalDate date);
}
