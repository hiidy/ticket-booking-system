package com.seatwise.show.repository;

import com.seatwise.show.domain.ShowSeat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

  @Query("SELECT ss from ShowSeat ss " + "JOIN FETCH ss.seat st " + "WHERE ss.show.id = :showId")
  List<ShowSeat> findByShowId(Long showId);

  @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.id IN :showIds")
  List<ShowSeat> findAllByShowId(List<Long> showIds);
}
