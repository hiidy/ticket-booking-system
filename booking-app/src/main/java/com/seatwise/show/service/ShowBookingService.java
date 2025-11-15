package com.seatwise.show.service;

import com.seatwise.booking.dto.BookingCreatedEvent;
import com.seatwise.booking.entity.Booking;
import com.seatwise.booking.entity.BookingRepository;
import com.seatwise.booking.exception.FatalBookingException;
import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.member.Member;
import com.seatwise.member.MemberRepository;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.TicketRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowBookingService {

  private final ApplicationEventPublisher eventPublisher;
  private final BookingRepository bookingRepository;
  private final TicketRepository ticketRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public String create(UUID requestId, Long memberId, List<Long> ticketIds) {
    if (bookingRepository.existsByRequestId(requestId)) {
      throw new FatalBookingException(ErrorCode.DUPLICATE_IDEMPOTENCY_KEY, requestId);
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new FatalBookingException(ErrorCode.MEMBER_NOT_FOUND, requestId));

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    List<Ticket> tickets = ticketRepository.findAllAvailableSeats(ticketIds, bookingRequestTime);

    if (tickets.size() != ticketIds.size()) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    boolean anyUnavailable =
        tickets.stream().anyMatch(ticket -> !ticket.canAssignBooking(bookingRequestTime));

    if (anyUnavailable) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    int totalAmount = tickets.stream().map(Ticket::getPrice).reduce(0, Integer::sum);
    Booking booking = Booking.success(requestId, member, totalAmount);
    Booking savedBooking = bookingRepository.save(booking);
    log.info("booking 저장 : {}", booking.getId());
    tickets.forEach(
        ticket ->
            ticket.assignBooking(savedBooking.getId(), bookingRequestTime, Duration.ofMinutes(10)));

    eventPublisher.publishEvent(new BookingCreatedEvent(ticketIds, memberId));
    return savedBooking.getId().toString();
  }

  @Transactional
  public String createWithLock(UUID requestId, Long memberId, List<Long> ticketIds) {
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
    Booking booking = Booking.success(requestId, member, totalAmount);
    Booking savedBooking = bookingRepository.save(booking);

    tickets.forEach(
        t -> t.assignBooking(savedBooking.getId(), bookingRequestTime, Duration.ofMinutes(10)));

    eventPublisher.publishEvent(new BookingCreatedEvent(ticketIds, memberId));

    return savedBooking.getId().toString();
  }

  @Transactional
  public void createFailedBooking(UUID requestId, Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new FatalBookingException(ErrorCode.MEMBER_NOT_FOUND, requestId));

    Booking booking = Booking.failed(requestId, member);
    bookingRepository.save(booking);
  }

  @Transactional
  public void cancel(Long memberId, Long bookingId) {
    List<Ticket> tickets = ticketRepository.findTicketsByBookingId(bookingId);

    if (tickets.isEmpty()) {
      throw new IllegalArgumentException("없는 티켓을 취소하려고합니다");
    }

    tickets.forEach(Ticket::cancelBooking);
    bookingRepository.deleteById(bookingId);
  }

  @Transactional
  public void cancelWithoutRefund(UUID requestId) {
    Optional<Booking> bookingOpt = bookingRepository.findByRequestId(requestId);

    if (bookingOpt.isEmpty()) {
      log.debug("정리할 예약이 없음: requestId={}", requestId);
      return;
    }

    Booking booking = bookingOpt.get();
    List<Ticket> tickets = ticketRepository.findTicketsByBookingId(booking.getId());

    tickets.forEach(Ticket::cancelBooking);
    bookingRepository.deleteById(booking.getId());

    log.info("만료된 예약 정리: requestId={}, bookingId={}", requestId, booking.getId());
  }
}
