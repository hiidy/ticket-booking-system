package com.seatwise.show.repository;

import com.seatwise.show.domain.ShowSeat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);
}
