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
@Transactional
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;

  public Long createBooking(Long showId, List<Long> seatIds) {
    List<ShowSeat> showSeats =
        seatIds.stream()
            .map(
                seatId ->
                    showSeatRepository
                        .findByShowIdAndSeatId(showId, seatId)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.SHOW_SEAT_NOT_FOUND)))
            .toList();

    Booking booking = Booking.builder().build();
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }
}
