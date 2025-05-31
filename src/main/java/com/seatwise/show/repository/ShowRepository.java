package com.seatwise.show.repository;

import com.seatwise.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {}
