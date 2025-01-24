package com.seatwise.booking.service;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.repository.BookingRepository;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.repository.ShowSeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;

  @Transactional
  public Long createBooking(List<Long> showSeatIds) {

    List<ShowSeat> showSeats = showSeatRepository.findAllByShowIdWithLock(showSeatIds);
    if (showSeats.isEmpty() || showSeats.size() != showSeatIds.size()) {
      throw new NotFoundException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }

    Booking booking = Booking.builder().build();
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }
}
