package com.seatwise.show.repository;

import com.seatwise.show.Show;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

  List<Show> findByEventId(Long eventId);
}
