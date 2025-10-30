package com.seatwise;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingStatus;
import com.booking.system.TicketAvro;
import com.booking.system.TicketStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

@Slf4j
@RequiredArgsConstructor
public class TicketBookingProcessor
    implements Processor<String, BookingCommandAvro, String, BookingAvro> {

  private final String storeName;
  private KeyValueStore<String, ValueAndTimestamp<TicketAvro>> ticketStore;
  private ProcessorContext<String, BookingAvro> context;

  @Override
  public void init(ProcessorContext<String, BookingAvro> context) {
    this.context = context;
    this.ticketStore = context.getStateStore(storeName);
  }

  @Override
  public void process(Record<String, BookingCommandAvro> record) {
    String sectionId = record.key();
    BookingCommandAvro command = record.value();

    Instant now = Instant.now();

    BookingAvro booking =
        BookingAvro.newBuilder()
            .setBookingId(command.getBookingId())
            .setMemberId(command.getMemberId())
            .setSectionId(command.getSectionId())
            .setTicketIds(command.getTicketIds())
            .setStatus(BookingStatus.PENDING)
            .setTimestamp(now)
            .build();

    List<Long> ticketIds = command.getTicketIds();
    List<TicketAvro> validTickets = new ArrayList<>();

    for (Long ticketId : ticketIds) {
      String key = ticketId.toString();
      ValueAndTimestamp<TicketAvro> valueAndTimestamp = ticketStore.get(ticketId.toString());

      if (valueAndTimestamp == null) {
        log.warn("Ticket NOT FOUND in store: ticketId={}, key='{}'", ticketId, key);
        continue;
      }

      TicketAvro ticket = valueAndTimestamp.value();

      // 1. 티켓 존재 여부
      if (ticket == null) {
        booking.setStatus(BookingStatus.FAILED);
        booking.setErrorMessage(String.format("Ticket not found: ticketId=%d", ticketId));
        context.forward(record.withKey(booking.getBookingId()).withValue(booking));
        return;
      }

      // 2. 티켓 예약 검증
      if (!isTicketAvailable(ticket, now)) {
        booking.setStatus(BookingStatus.FAILED);
        booking.setErrorMessage(
            String.format(
                "Ticket not available: ticketId=%d, status=%s", ticketId, ticket.getStatus()));
        context.forward(record.withKey(booking.getBookingId()).withValue(booking));
        return;
      }

      validTickets.add(ticket);
    }

    booking.setStatus(BookingStatus.BOOKED);

    long totalAmount = 0L;

    for (int i = 0; i < validTickets.size(); i++) {
      TicketAvro ticket = validTickets.get(i);
      Long ticketId = ticketIds.get(i);

      TicketAvro updatedTicket =
          TicketAvro.newBuilder(ticket)
              .setStatus(TicketStatus.PAYMENT_PENDING)
              .setExpirationTime(now.plus(Duration.ofMinutes(5)))
              .setBookingId(command.getBookingId())
              .build();

      ticketStore.put(
          ticketId.toString(), ValueAndTimestamp.make(updatedTicket, now.toEpochMilli()));
      totalAmount += ticket.getPrice();
    }

    booking.setTotalAmount(totalAmount);

    context.forward(record.withKey(booking.getBookingId()).withValue(booking));
  }

  @Override
  public void close() {
    Processor.super.close();
  }

  private boolean isTicketAvailable(TicketAvro ticket, Instant now) {
    if (ticket.getStatus() == TicketStatus.AVAILABLE) {
      return ticket.getExpirationTime() == null;
    }

    if (ticket.getStatus() == TicketStatus.PAYMENT_PENDING) {
      return ticket.getExpirationTime() != null && ticket.getExpirationTime().isBefore(now);
    }

    return false;
  }
}
