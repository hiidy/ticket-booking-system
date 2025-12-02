package com.seatwise.show.service;

import com.seatwise.booking.BookingService;
import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.member.MemberRepository;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.TicketRepository;
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
public class ShowBookingService {

  private final BookingService bookingService;
  private final TicketRepository ticketRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public String create(UUID requestId, Long memberId, List<Long> ticketIds) {
    // 1. 멤버 존재 여부 확인
    if (!memberRepository.existsById(memberId)) {
      throw new BusinessException(BaseCode.MEMBER_NOT_FOUND);
    }

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    // 2. 티켓 가용성 확인
    List<Ticket> tickets = ticketRepository.findAllAvailableSeats(ticketIds, bookingRequestTime);
    validateTicketAvailability(tickets, ticketIds, bookingRequestTime, requestId);

    // 3. 예매 생성
    int totalAmount = tickets.stream().mapToInt(Ticket::getPrice).sum();
    Long savedBookingId = bookingService.createBooking(requestId, memberId, totalAmount);

    // 4. 티켓 상태 변경
    tickets.forEach(
        ticket -> ticket.assignBooking(savedBookingId, bookingRequestTime, Duration.ofMinutes(10)));

    return savedBookingId.toString();
  }

  private void validateTicketAvailability(
      List<Ticket> tickets,
      List<Long> ticketIds,
      LocalDateTime bookingRequestTime,
      UUID requestId) {
    if (tickets.size() != ticketIds.size()) {
      throw new BusinessException(BaseCode.SEAT_NOT_AVAILABLE);
    }

    boolean anyUnavailable =
        tickets.stream().anyMatch(ticket -> !ticket.canAssignBooking(bookingRequestTime));

    if (anyUnavailable) {
      throw new BusinessException(BaseCode.SEAT_NOT_AVAILABLE);
    }
  }

  @Transactional
  public String createWithLock(UUID requestId, Long memberId, List<Long> ticketIds) {
    // 1. 멤버 존재 여부 확인
    if (!memberRepository.existsById(memberId)) {
      throw new BusinessException(BaseCode.MEMBER_NOT_FOUND);
    }

    LocalDateTime bookingRequestTime = LocalDateTime.now();

    // 2. 락 획득 및 티켓 확인
    List<Ticket> tickets;
    try {
      tickets = ticketRepository.findAllAvailableSeatsWithLock(ticketIds, bookingRequestTime);
    } catch (org.springframework.dao.PessimisticLockingFailureException e) {
      throw new BusinessException(BaseCode.SEAT_NOT_AVAILABLE);
    }

    // 3. 티켓 가용성 검증
    if (tickets.size() != ticketIds.size()
        || tickets.stream().anyMatch(t -> !t.canAssignBooking(bookingRequestTime))) {
      throw new BusinessException(BaseCode.SEAT_NOT_AVAILABLE);
    }

    // 4. 예매 생성
    int totalAmount = tickets.stream().mapToInt(Ticket::getPrice).sum();
    Long savedBookingId = bookingService.createBooking(requestId, memberId, totalAmount);

    // 5. 티켓 상태 변경
    tickets.forEach(
        t -> t.assignBooking(savedBookingId, bookingRequestTime, Duration.ofMinutes(10)));

    return savedBookingId.toString();
  }

  @Transactional
  public void createFailedBooking(UUID requestId, Long memberId) {
    if (!memberRepository.existsById(memberId)) {
      throw new BusinessException(BaseCode.MEMBER_NOT_FOUND);
    }

    bookingService.createFailedBooking(requestId, memberId);
  }

  @Transactional
  public void cancelTickets(Long bookingId) {
    List<Ticket> tickets = ticketRepository.findTicketsByBookingId(bookingId);

    if (tickets.isEmpty()) {
      throw new IllegalArgumentException("없는 티켓을 취소하려고합니다");
    }

    tickets.forEach(Ticket::cancelBooking);
  }

  @Transactional
  public void cancelWithoutRefund(UUID requestId) {
    // TODO: 만료된 예매 정리 로직 구현 필요
    log.debug("만료된 예약 정리 요청: requestId={}", requestId);
  }
}
