package com.seatwise.booking;

import com.seatwise.booking.dto.response.BookingStatusResponse;
import com.seatwise.booking.entity.Booking;
import com.seatwise.booking.entity.BookingRepository;
import com.seatwise.booking.entity.BookingStatus;
import com.seatwise.booking.exception.FatalBookingException;
import com.seatwise.core.BaseCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;

  @Transactional(propagation = Propagation.MANDATORY)
  public Long createBooking(UUID requestId, Long memberId, int totalAmount) {
    if (bookingRepository.existsByRequestId(requestId)) {
      throw new FatalBookingException(BaseCode.DUPLICATE_IDEMPOTENCY_KEY, requestId);
    }

    Booking booking = Booking.createNew(requestId, memberId, totalAmount);

    booking.markAsSuccess();

    return bookingRepository.save(booking).getId();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public Long createFailedBooking(UUID requestId, Long memberId) {
    if (bookingRepository.existsByRequestId(requestId)) {
      throw new FatalBookingException(BaseCode.DUPLICATE_IDEMPOTENCY_KEY, requestId);
    }

    Booking booking = Booking.createNew(requestId, memberId, 0);

    booking.markAsFailed();

    return bookingRepository.save(booking).getId();
  }

  public BookingStatusResponse getBookingStatus(UUID requestId) {
    Booking booking = bookingRepository.findByRequestId(requestId).orElseThrow();

    if (booking.getStatus() == BookingStatus.SUCCESS) {
      return BookingStatusResponse.success(booking.getId().toString(), requestId);
    }
    return BookingStatusResponse.failed(requestId);
  }
}
