package com.seatwise.show.service.strategy;

import com.seatwise.show.dto.request.ShowBookingRequest;
import java.util.UUID;

public interface ShowBookingStrategy {

  String createBooking(UUID idempotencyKey, ShowBookingRequest request);
}
