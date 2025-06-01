package com.seatwise.booking;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.domain.BookingRepository;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.messaging.BookingMessageProducer;
import com.seatwise.common.exception.BookingException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.showtime.domain.ShowSeat;
import com.seatwise.showtime.domain.ShowSeatRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;
  private final MemberRepository memberRepository;
  private final BookingMessageProducer producer;

  @Transactional
  public Long createBooking(UUID requestId, Long memberId, List<Long> showSeatIds) {
    if (bookingRepository.existsByRequestId(requestId)) {
      throw new BookingException(ErrorCode.DUPLICATE_IDEMPOTENCY_KEY, requestId);
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new BookingException(ErrorCode.MEMBER_NOT_FOUND, requestId));

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    List<ShowSeat> showSeats =
        showSeatRepository.findAllAvailableSeats(showSeatIds, bookingRequestTime);

    if (showSeats.size() != showSeatIds.size()) {
      throw new BookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    boolean anyUnavailable =
        showSeats.stream().anyMatch(seat -> !seat.canAssignBooking(bookingRequestTime));

    if (anyUnavailable) {
      throw new BookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    int totalAmount = showSeats.stream().map(ShowSeat::getPrice).reduce(0, Integer::sum);
    Booking booking = new Booking(requestId, member, totalAmount);
    showSeats.forEach(
        showSeat -> showSeat.assignBooking(booking, bookingRequestTime, Duration.ofMinutes(10)));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }

  public void enqueueBooking(UUID requestId, BookingRequest request) {
    BookingMessage message =
        new BookingMessage(
            requestId.toString(), request.memberId(), request.showSeatIds(), request.sectionId());
    producer.sendMessage(message);
  }
}
