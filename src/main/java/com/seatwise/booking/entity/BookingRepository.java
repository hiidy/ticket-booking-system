package com.seatwise.booking.entity;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  boolean existsByRequestId(UUID requestId);
}
