package com.seatwise.event.repository;

import com.seatwise.event.domain.Event;
import com.seatwise.event.domain.EventType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findAllByType(EventType type, Pageable pageable);
}
