package com.seatwise.ticket.domain;

import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

  @Query(
      "SELECT ss from Ticket ss " + "JOIN FETCH ss.seat st " + "WHERE ss.showTime.id = :showTimeId")
  List<Ticket> findAllByShowTimeId(Long showTimeId);

  @Query(
      """
      select new com.seatwise.showtime.dto.response.SeatAvailabilityResponse(
              s.grade,
              count(ss),
              sum(case when ss.status in ('AVAILABLE','PAYMENT_PENDING') then 1 else 0 end)
      )
      from Ticket ss
      join ss.seat s
      where ss.showTime.id = :showTimeId
      group by s.grade
      """)
  List<SeatAvailabilityResponse> findSeatAvailabilityByShowId(@Param("showTimeId") Long showTimeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT ss FROM Ticket ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
  List<Ticket> findAllAvailableSeatsWithLock(List<Long> showSeatIds, LocalDateTime currentTime);

  @Query(
      "SELECT ss FROM Ticket ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  List<Ticket> findAllAvailableSeats(List<Long> showSeatIds, LocalDateTime currentTime);
}
