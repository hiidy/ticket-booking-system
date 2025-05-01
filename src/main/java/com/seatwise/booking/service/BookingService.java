package com.seatwise.booking.service;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.dto.BookingResult;
import com.seatwise.booking.repository.BookingRedisRepository;
import com.seatwise.booking.repository.BookingRepository;
import com.seatwise.common.exception.BadRequestException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.member.domain.Member;
import com.seatwise.member.repository.MemberRepository;
import com.seatwise.queue.dto.BookingMessage;
import com.seatwise.queue.service.BookingMessageProducer;
import com.seatwise.show.domain.ShowSeat;
import com.seatwise.show.repository.ShowSeatRepository;
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
  private final BookingRedisRepository bookingRedisRepository;
  private final BookingMessageProducer producer;

  @Transactional
  public Long createBooking(String requestId, Long memberId, List<Long> showSeatIds) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    LocalDateTime bookingRequestTime = LocalDateTime.now();
    List<ShowSeat> showSeats =
        showSeatRepository.findAllAvailableSeats(showSeatIds, bookingRequestTime);

    if (showSeats.size() != showSeatIds.size()) {
      throw new BadRequestException(ErrorCode.SEAT_NOT_AVAILABLE);
    }

    int totalAmount = showSeats.stream().map(ShowSeat::getPrice).reduce(0, Integer::sum);
    Booking booking = new Booking(requestId, member, totalAmount);
    showSeats.forEach(showSeat -> showSeat.assignBooking(booking, bookingRequestTime));
    Booking savedBooking = bookingRepository.save(booking);

    return savedBooking.getId();
  }

  public void enqueueBooking(String requestId, BookingRequest request) {
    BookingMessage message =
        new BookingMessage(
            requestId, request.memberId(), request.showSeatIds(), request.sectionId());
    producer.sendMessage(message);
  }

  public BookingResult readBookingResult(String requestId) {
    int tries = 30;
    int delayMs = 1;
    BookingResult result = null;

    while (tries-- > 0) {
      result = bookingRedisRepository.getBookingResult(requestId);

      if (result != null) {
        return result;
      }

      try {
        Thread.sleep(delayMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    return null;
  }
}
