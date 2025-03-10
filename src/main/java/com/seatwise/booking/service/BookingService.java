package com.seatwise.booking.service;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.repository.BookingRepository;
import com.seatwise.booking.repository.RedisBookingRepository;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.member.domain.Member;
import com.seatwise.member.repository.MemberRepository;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.repository.ShowSeatRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;
  private final MemberRepository memberRepository;
  private final RedisBookingRepository redisBookingRepository;

  @Transactional
  public Long createBooking(Long memberId, List<Long> showSeatIds) {
    boolean allLocked = redisBookingRepository.lockSeat(memberId, showSeatIds);

    if (!allLocked) {
      throw new BadRequestException(ErrorCode.SEAT_NOT_AVAILABLE);
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    List<ShowSeat> showSeats = showSeatRepository.findAllByIds(showSeatIds);
    if (showSeats.isEmpty() || showSeats.size() != showSeatIds.size()) {
      throw new NotFoundException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }

    Booking booking = new Booking(member);
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking, LocalDateTime.now()));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }
}
