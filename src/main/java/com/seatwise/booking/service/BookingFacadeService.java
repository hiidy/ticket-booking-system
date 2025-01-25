package com.seatwise.booking.service;

import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingFacadeService {

  private final BookingService bookingService;

  public Long createBookingFacade(Long memberId, List<Long> showSeatIds) {
    try {
      return bookingService.createBooking(memberId, showSeatIds);
    } catch (OptimisticLockingFailureException e) {
      throw new BadRequestException(ErrorCode.SEAT_ALREADY_BOOKED);
    }
  }
}
