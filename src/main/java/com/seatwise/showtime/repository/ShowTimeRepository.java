package com.seatwise.showtime.repository;

import com.seatwise.event.entity.EventType;
import com.seatwise.showtime.dto.response.ShowSummaryQueryDto;
import com.seatwise.showtime.entity.ShowTime;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowTimeRepository
    extends JpaRepository<ShowTime, Long>, JpaSpecificationExecutor<ShowTime> {

  List<ShowTime> findByEventId(Long eventId);

  List<ShowTime> findByEventIdAndDateBetween(Long eventId, LocalDate startDate, LocalDate endDate);

  List<ShowTime> findShowTimesByEventIdAndDate(Long eventId, LocalDate date);

  @Query(
      """
              select new com.seatwise.showtime.dto.response.ShowSummaryQueryDto(s.id, e.title, e.type, s.date, s.startTime, v.name)
              from ShowTime s
              join s.event e
              join s.venue v
              where e.type = :type and s.date > :date
              order by s.date asc
          """)
  Slice<ShowSummaryQueryDto> findShowSummaryByTypeAndDate(
      @Param("type") EventType type, @Param("date") LocalDate date, Pageable pageable);
}
