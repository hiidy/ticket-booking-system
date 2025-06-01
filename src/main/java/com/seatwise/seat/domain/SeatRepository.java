package com.seatwise.seat.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

  List<Seat> findByIdBetween(Long startId, Long endId);

  List<Seat> findByVenueId(Long venueId);
}
