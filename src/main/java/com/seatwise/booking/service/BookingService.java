package com.seatwise.booking.service;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.repository.BookingRepository;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.member.domain.Member;
import com.seatwise.member.repository.MemberRepository;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.repository.ShowRepository;
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
  private final MemberRepository memberRepository;
  private final ShowRepository showRepository;
  private final SeatRepository seatRepository;

  @Transactional
  public Long createBooking(Long memberId, List<Long> showSeatIds) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    List<ShowSeat> showSeats = showSeatRepository.findAllByIdWithLock(showSeatIds);
    if (showSeats.isEmpty() || showSeats.size() != showSeatIds.size()) {
      throw new NotFoundException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }

    Booking booking = new Booking(member);
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }

  @Transactional
  public Long createBooking2(Long memberId, Long showId, List<Long> seatIds) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    if (showSeatRepository.existsByShowIdAndSeatIdIn(showId, seatIds)) {
      throw new NotFoundException(ErrorCode.SEAT_ALREADY_BOOKED);
    }

    Show show = showRepository.findById(showId).orElseThrow();
    List<Seat> seats = seatRepository.findAllById(seatIds);

    ShowSeat showSeat = ShowSeat.createAvailable(show, seats.get(0), 40000);
    ShowSeat save = showSeatRepository.save(showSeat);
    return save.getId();
  }
}
