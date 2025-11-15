package com.seatwise.booking;

import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.booking.entity.Booking;
import com.seatwise.booking.entity.BookingRepository;
import com.seatwise.booking.entity.BookingStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;

  public BookingStatusResponse getBookingStatus(UUID requestId) {
    Booking booking = bookingRepository.findByRequestId(requestId).orElseThrow();

    if (booking.getStatus() == BookingStatus.SUCCESS) {
      return BookingStatusResponse.success(booking.getId().toString(), requestId);
    }
    return BookingStatusResponse.failed(requestId);
  }
}
