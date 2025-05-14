package com.seatwise.event.repository;

import com.seatwise.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {}
