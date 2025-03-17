package com.seatwise.show.repository;

import com.seatwise.show.domain.ShowSeat;
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

  @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id IN :showIds")
  List<ShowSeat> findAllByShowIds(List<Long> showIds);

  List<ShowSeat> findAllById(Iterable<Long> showSeatIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds AND (ss.expirationTime IS NULL OR ss.expirationTime > :currentTime) AND ss.status != 'BOOKED'")
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0")})
  List<ShowSeat> findAllAvailableSeatsWithLock(List<Long> showSeatIds, LocalDateTime currentTime);
}
