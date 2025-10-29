package com.seatwise.booking;

import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketAvro;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

@RequiredArgsConstructor
public class BookingRequestProcessor
    implements FixedKeyProcessor<String, BookingRequestAvro, BookingCommandAvro> {

  private final String storeName;
  private ReadOnlyKeyValueStore<String, TicketAvro> ticketCache;
  private FixedKeyProcessorContext<String, BookingCommandAvro> context;

  @Override
  public void init(FixedKeyProcessorContext<String, BookingCommandAvro> context) {
    this.context = context;
    this.ticketCache = context.getStateStore(storeName);
  }

  @Override
  public void process(FixedKeyRecord<String, BookingRequestAvro> requestRecord) {
    BookingRequestAvro request = requestRecord.value();

    // 캐시 검증
    if (!validateTicketsInCache(request.getTicketIds())) {
      return;
    }

    BookingCommandAvro booking =
        BookingCommandAvro.newBuilder()
            .setBookingId(request.getRequestId())
            .setSectionId(request.getSectionId())
            .setTicketIds(request.getTicketIds())
            .setMemberId(request.getMemberId())
            .build();
    context.forward(requestRecord.withValue(booking));
  }

  private boolean validateTicketsInCache(List<Long> ticketIds) {
    for (Long ticketId : ticketIds) {
      TicketAvro ticket = ticketCache.get(ticketId.toString());

      if (ticket == null) {
        continue;
      }

      if (!"AVAILABLE".equals(ticket.getStatus())) {
        return false;
      }

      Instant curTime = Instant.now();
      if (ticket.getExpirationTime() != null && curTime.isBefore(ticket.getExpirationTime())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void close() {
    FixedKeyProcessor.super.close();
  }
}
