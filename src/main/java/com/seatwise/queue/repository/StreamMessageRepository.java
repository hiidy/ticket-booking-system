package com.seatwise.queue.repository;

import com.seatwise.queue.entity.StreamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamMessageRepository extends JpaRepository<StreamMessage, Long> {}
