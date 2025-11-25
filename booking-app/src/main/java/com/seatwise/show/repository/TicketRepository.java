package com.seatwise.show.repository;

import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.entity.Ticket;
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

  @Query("SELECT t FROM Ticket t JOIN FETCH t.seat s WHERE t.show.id = :showId")
  List<Ticket> findAllByShowId(@Param("showId") Long showId);

  @Query(
      """
              SELECT new com.seatwise.show.dto.response.SeatAvailabilityResponse(
                  s.rowName,
                  COUNT(t),
                  SUM(CASE WHEN t.status IN ('AVAILABLE','PAYMENT_PENDING') THEN 1 ELSE 0 END)
              )
              FROM Ticket t
              JOIN t.seat s
              WHERE t.show.id = :showId
              GROUP BY s.rowName
              """)
  List<SeatAvailabilityResponse> findTicketAvailabilityByShowId(@Param("showId") Long showId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT t FROM Ticket t WHERE t.id IN :ticketIds AND (t.expirationTime IS NULL OR t.expirationTime > :currentTime) AND t.status != 'BOOKED'")
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
  List<Ticket> findAllAvailableSeatsWithLock(
      @Param("ticketIds") List<Long> ticketIds, @Param("currentTime") LocalDateTime currentTime);

  @Query(
      "SELECT t FROM Ticket t WHERE t.id IN :ticketIds AND (t.expirationTime IS NULL OR t.expirationTime > :currentTime) AND t.status != 'BOOKED'")
  List<Ticket> findAllAvailableSeats(
      @Param("ticketIds") List<Long> ticketIds, @Param("currentTime") LocalDateTime currentTime);

  List<Ticket> findTicketsByBookingId(Long id);

  @Query(
      "SELECT t FROM Ticket t JOIN FETCH t.seat s WHERE t.show.id = :showId AND t.sectionId = :sectionId")
  List<Ticket> findTicketsByShowIdAndSectionId(Long showId, Long sectionId);
}
