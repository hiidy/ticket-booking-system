package com.seatwise.booking;

import com.seatwise.booking.domain.Booking;
import com.seatwise.booking.domain.BookingRepository;
import com.seatwise.booking.dto.BookingMessage;
import com.seatwise.booking.dto.BookingRequest;
import com.seatwise.booking.exception.BookingException;
import com.seatwise.booking.messaging.BookingMessageProducer;
import com.seatwise.core.ErrorCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketRepository;
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
  private final TicketRepository ticketRepository;
  private final MemberRepository memberRepository;
  private final BookingMessageProducer producer;

  @Transactional
  public Long createBooking(UUID requestId, Long memberId, List<Long> ticketIds) {
    if (bookingRepository.existsByRequestId(requestId)) {
      throw new BookingException(ErrorCode.DUPLICATE_IDEMPOTENCY_KEY, requestId);
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new BookingException(ErrorCode.MEMBER_NOT_FOUND, requestId));

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    List<Ticket> tickets = ticketRepository.findAllAvailableSeats(ticketIds, bookingRequestTime);

    if (tickets.size() != ticketIds.size()) {
      throw new BookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    boolean anyUnavailable =
        tickets.stream().anyMatch(seat -> !seat.canAssignBooking(bookingRequestTime));

    if (anyUnavailable) {
      throw new BookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    int totalAmount = tickets.stream().map(Ticket::getPrice).reduce(0, Integer::sum);
    Booking booking = new Booking(requestId, member, totalAmount);
    Booking savedBooking = bookingRepository.save(booking);
    tickets.forEach(
        ticket ->
            ticket.assignBooking(savedBooking.getId(), bookingRequestTime, Duration.ofMinutes(10)));

    return savedBooking.getId();
  }

  public void enqueueBooking(UUID requestId, BookingRequest request) {
    BookingMessage message =
        new BookingMessage(
            requestId.toString(), request.memberId(), request.ticketIds(), request.sectionId());
    producer.sendMessage(message);
  }
}
