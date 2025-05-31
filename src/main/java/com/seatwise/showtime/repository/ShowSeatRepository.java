package com.seatwise.showtime.repository;

import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import com.seatwise.showtime.entity.ShowSeat;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  @Query(
      "SELECT ss from ShowSeat ss " + "JOIN FETCH ss.seat st " + "WHERE ss.showTime.id = :showId")
  List<ShowSeat> findAllByShowId(Long showId);

  @Query(
      value =
          "SELECT "
              + "st.grade AS grade, "
              + "COUNT(ss.id) AS totalCount, "
              + "SUM(CASE WHEN ss.status IN ('AVAILABLE', 'PAYMENT_PENDING') THEN 1 ELSE 0 END) AS availableCount "
              + "FROM show_seat ss "
              + "JOIN seat st ON ss.seat_id = st.id "
              + "WHERE ss.show_time_id = :showTimeId "
              + "GROUP BY st.grade",
      nativeQuery = true)
  List<SeatAvailabilityResponse> findSeatAvailabilityByShowId(@Param("showTimeId") Long showTimeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
  List<ShowSeat> findAllAvailableSeatsWithLock(List<Long> showSeatIds, LocalDateTime currentTime);

  @Query(
      "SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  List<ShowSeat> findAllAvailableSeats(List<Long> showSeatIds, LocalDateTime currentTime);
}
