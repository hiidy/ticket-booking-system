package com.seatwise.showtime.repository;

import com.seatwise.show.entity.ShowType;
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

  List<ShowTime> findByShowId(Long showId);

  List<ShowTime> findByShowIdAndDateBetween(Long showId, LocalDate startDate, LocalDate endDate);

  List<ShowTime> findShowTimesByShowIdAndDate(Long showId, LocalDate date);

  @Query(
      """
              select new com.seatwise.showtime.dto.response.ShowSummaryQueryDto(s.id, s.title, s.type, st.date, st.startTime, v.name)
              from ShowTime st
              join st.show s
              join st.venue v
              where s.type = :type and st.date > :date
              order by st.date asc
          """)
  Slice<ShowSummaryQueryDto> findShowSummaryByTypeAndDate(
      @Param("type") ShowType type, @Param("date") LocalDate date, Pageable pageable);
}
