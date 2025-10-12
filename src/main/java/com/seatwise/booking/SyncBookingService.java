package com.seatwise.booking;

import com.seatwise.booking.exception.RecoverableBookingException;
import com.seatwise.core.ErrorCode;
import com.seatwise.ticket.TicketCacheService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBookingService {

  private final TicketCacheService cacheService;
  private final BookingTransactionalService transactionalService;

  public Long createBookingSync(UUID requestId, Long memberId, List<Long> ticketIds) {
    if (cacheService.hasUnavailableTickets(ticketIds, memberId)) {
      throw new RecoverableBookingException(ErrorCode.SEAT_NOT_AVAILABLE, requestId);
    }

    return transactionalService.createBookingInTransaction(requestId, memberId, ticketIds);
  }
}
