package com.seatwise.booking.strategy;

import com.seatwise.booking.dto.request.BookingRequest;
import com.seatwise.booking.dto.response.BookingStatusResponse;
import java.util.UUID;

public interface BookingStrategy {

  BookingStatusResponse createBooking(UUID idempotencyKey, BookingRequest request);
}
