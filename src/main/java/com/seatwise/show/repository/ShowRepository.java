package com.seatwise.show.repository;

import com.seatwise.event.domain.EventType;
import com.seatwise.show.domain.Show;
import com.seatwise.show.dto.response.ShowSummaryQueryDto;
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
public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

  List<Show> findByEventId(Long eventId);

  List<Show> findByEventIdAndDateBetween(Long eventId, LocalDate startDate, LocalDate endDate);

  List<Show> findShowsByEventIdAndDate(Long eventId, LocalDate date);

  @Query(
      """
              select new com.seatwise.show.dto.response.ShowSummaryQueryDto(s.id, e.title, e.type, s.date, s.startTime, v.name)
              from Show s
              join s.event e
              join s.venue v
              where e.type = :type and s.date > :date
              order by s.date asc
          """)
  Slice<ShowSummaryQueryDto> findShowSummaryByTypeAndDate(
      @Param("type") EventType type, @Param("date") LocalDate date, Pageable pageable);
}
