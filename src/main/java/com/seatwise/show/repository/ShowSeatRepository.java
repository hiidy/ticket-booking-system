package com.seatwise.show.repository;

import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

  @Query("SELECT ss from ShowSeat ss " + "JOIN FETCH ss.seat st " + "WHERE ss.show.id = :showId")
  List<ShowSeat> findAllByShowId(Long showId);

  @Query(
      value =
          "SELECT "
              + "st.grade AS grade, "
              + "COUNT(ss.id) AS totalCount, "
              + "SUM(CASE WHEN ss.status IN ('AVAILABLE', 'PAYMENT_PENDING') THEN 1 ELSE 0 END) AS availableCount "
              + "FROM show_seat ss "
              + "JOIN seat st ON ss.seat_id = st.id "
              + "WHERE ss.show_id = :showId "
              + "GROUP BY st.grade",
      nativeQuery = true)
  List<SeatAvailabilityResponse> findSeatAvailabilityByShowId(@Param("showId") Long showId);

  @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id IN :showIds")
  List<ShowSeat> findAllByShowIds(List<Long> showIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
  List<ShowSeat> findAllAvailableSeatsWithLock(List<Long> showSeatIds, LocalDateTime currentTime);

  @Query(
      "SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  List<ShowSeat> findAllAvailableSeats(List<Long> showSeatIds, LocalDateTime currentTime);
}
