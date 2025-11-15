package com.seatwise.show.service;

import com.seatwise.booking.dto.BookingCreatedEvent;
import com.seatwise.show.cache.TicketCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

  private final TicketCacheService cacheService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBookingCreated(BookingCreatedEvent event) {
    cacheService.holdTickets(event.ticketIds(), event.memberId());
  }
}
