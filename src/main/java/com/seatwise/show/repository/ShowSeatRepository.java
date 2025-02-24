package com.seatwise.show.repository;

import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.domain.Status;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

  @Query("SELECT ss from ShowSeat ss " + "JOIN FETCH ss.seat st " + "WHERE ss.show.id = :showId")
  List<ShowSeat> findByShowId(Long showId);

  @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id IN :showIds")
  List<ShowSeat> findAllByShowId(List<Long> showIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT ss FROM ShowSeat ss WHERE ss.id IN :showSeatIds")
  List<ShowSeat> findAllByIdWithLock(List<Long> showSeatIds);

  List<ShowSeat> findAllByStatusIsAndExpirationTimeBefore(
      Status status, LocalDateTime expirationTime);
}
