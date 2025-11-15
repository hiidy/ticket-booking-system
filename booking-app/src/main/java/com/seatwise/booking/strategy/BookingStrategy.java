package com.seatwise.booking.strategy;

import com.seatwise.booking.dto.request.BookingRequest;
import java.util.UUID;

public interface BookingStrategy {

  String createBooking(UUID idempotencyKey, BookingRequest request);
}
