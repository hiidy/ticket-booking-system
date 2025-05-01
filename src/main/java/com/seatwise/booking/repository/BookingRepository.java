package com.seatwise.booking.repository;

import com.seatwise.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  boolean existsByRequestId(String requestId);
}
