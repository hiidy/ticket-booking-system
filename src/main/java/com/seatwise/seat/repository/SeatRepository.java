package com.seatwise.seat.repository;

import com.seatwise.seat.domain.Seat;
import com.seatwise.show.domain.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

  List<Seat> findByIdBetween(Long startId, Long endId);

  @Query(
      "SELECT s FROM Seat s "
          + "JOIN s.showSeats ss "
          + "where ss.show.id = :showId and ss.status = :status")
  List<Seat> findByShowIdAndStatus(@Param("showId") Long showId, @Param("status") Status status);
}
