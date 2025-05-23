package com.seatwise.queue.repository;

import com.seatwise.queue.domain.StreamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamMessageRepository extends JpaRepository<StreamMessage, Long> {}
