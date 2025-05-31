package com.seatwise.booking.repository;

import com.seatwise.booking.entity.Booking;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  boolean existsByRequestId(UUID requestId);
}
