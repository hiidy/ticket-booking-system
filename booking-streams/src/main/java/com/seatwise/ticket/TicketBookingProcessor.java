package com.seatwise.ticket;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingStatus;
import com.booking.system.TicketAvro;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

@RequiredArgsConstructor
public class TicketBookingProcessor
    implements Processor<String, BookingCommandAvro, String, BookingAvro> {

  private final String storeName;
  private KeyValueStore<String, TicketAvro> ticketStore;
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

    BookingAvro booking =
        BookingAvro.newBuilder()
            .setBookingId(command.getBookingId())
            .setMemberId(command.getMemberId())
            .setSectionId(command.getSectionId())
            .setTicketIds(command.getTicketIds())
            .setStatus(BookingStatus.PENDING)
            .setTimestamp(Instant.now())
            .build();

    List<Long> ticketIds = command.getTicketIds();
    List<TicketAvro> validTickets = new ArrayList<>();

    for (Long ticketId : ticketIds) {
      TicketAvro ticket = ticketStore.get(ticketId.toString());

      // 1. 티켓 존재 여부
      if (ticket == null) {
        booking.setStatus(BookingStatus.FAILED);
        booking.setErrorMessage(String.format("Ticket not found: ticketId=%d", ticketId));
        context.forward(record.withKey(booking.getBookingId()).withValue(booking));
        return;
      }

      // 2. 티켓 상태 확인
      if (!"AVAILABLE".equals(ticket.getStatus())) {
        booking.setStatus(BookingStatus.FAILED);
        booking.setErrorMessage(
            String.format(
                "Ticket not available: ticketId=%d, status=%s", ticketId, ticket.getStatus()));
        context.forward(record.withKey(booking.getBookingId()).withValue(booking));
        return;
      }

      // 3. 만료 시간 확인
      if (ticket.getExpirationTime() != null && Instant.now().isAfter(ticket.getExpirationTime())) {
        booking.setStatus(BookingStatus.FAILED);
        booking.setErrorMessage(
            String.format(
                "Ticket expired: ticketId=%d, expirationTime=%s",
                ticketId, ticket.getExpirationTime()));
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
              .setStatus("BOOKED")
              .setBookingId(Long.parseLong(command.getBookingId()))
              .build();

      ticketStore.put(ticketId.toString(), updatedTicket);
      totalAmount += ticket.getPrice();
    }

    booking.setTotalAmount(totalAmount);

    context.forward(record.withKey(booking.getBookingId()).withValue(booking));
  }

  @Override
  public void close() {
    Processor.super.close();
  }
}
