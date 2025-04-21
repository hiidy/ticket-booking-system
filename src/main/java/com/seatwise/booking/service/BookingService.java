package com.seatwise.booking.service;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.repository.BookingRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public Long createBooking(Long memberId, List<Long> showSeatIds) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    LocalDateTime bookingRequestTime = LocalDateTime.now();
    List<ShowSeat> showSeats = showSeatRepository.findAllAvailableSeats(showSeatIds, bookingRequestTime);

    if (showSeats.size() != showSeatIds.size()) {
      throw new BadRequestException(ErrorCode.SEAT_NOT_AVAILABLE);
    }

    int totalAmount = showSeats.stream().map(ShowSeat::getPrice).reduce(0, Integer::sum);
    Booking booking = new Booking(member, totalAmount);
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking, bookingRequestTime));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }
}
