package com.seatwise.booking.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStreamMessageRepository extends JpaRepository<BookingStreamMessage, Long> {}
