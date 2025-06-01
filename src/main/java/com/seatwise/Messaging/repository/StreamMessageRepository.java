package com.seatwise.Messaging.repository;

import com.seatwise.Messaging.entity.StreamMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamMessageRepository extends JpaRepository<StreamMessage, Long> {}
