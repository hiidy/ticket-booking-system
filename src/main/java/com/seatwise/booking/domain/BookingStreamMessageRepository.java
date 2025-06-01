package com.seatwise.booking.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStreamMessageRepository extends JpaRepository<BookingStreamMessage, Long> {}
