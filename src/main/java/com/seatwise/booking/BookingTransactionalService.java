package com.seatwise.booking;

import com.seatwise.booking.dto.BookingCreatedEvent;
import com.seatwise.booking.entity.Booking;
import com.seatwise.booking.entity.BookingRepository;
import com.seatwise.booking.exception.FatalBookingException;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.ticket.Ticket;
import com.seatwise.ticket.TicketRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTransactionalService {

  private final ApplicationEventPublisher eventPublisher;
  private final BookingRepository bookingRepository;
  private final TicketRepository ticketRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public Long createBookingInTransaction(UUID requestId, Long memberId, List<Long> ticketIds) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new FatalBookingException(ErrorCode.MEMBER_NOT_FOUND, requestId));

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    List<Ticket> tickets;
    try {
      tickets = ticketRepository.findAllAvailableSeatsWithLock(ticketIds, bookingRequestTime);
    } catch (org.springframework.dao.PessimisticLockingFailureException e) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    if (tickets.size() != ticketIds.size()
        || tickets.stream().anyMatch(t -> !t.canAssignBooking(bookingRequestTime))) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    int totalAmount = tickets.stream().mapToInt(Ticket::getPrice).sum();
    Booking booking = new Booking(requestId, member, totalAmount);
    Booking savedBooking = bookingRepository.save(booking);

    tickets.forEach(
        t -> t.assignBooking(savedBooking.getId(), bookingRequestTime, Duration.ofMinutes(10)));

    eventPublisher.publishEvent(new BookingCreatedEvent(ticketIds, memberId));

    return savedBooking.getId();
  }
}
