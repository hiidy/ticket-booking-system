package com.seatwise;

import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketAvro;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

@Slf4j
@RequiredArgsConstructor
public class BookingRequestProcessor
    implements FixedKeyProcessor<String, BookingRequestAvro, BookingCommandAvro> {

  private final String storeName;
  private ReadOnlyKeyValueStore<String, ValueAndTimestamp<TicketAvro>> ticketCache;
  private FixedKeyProcessorContext<String, BookingCommandAvro> context;

  @Override
  public void init(FixedKeyProcessorContext<String, BookingCommandAvro> context) {
    this.context = context;
    this.ticketCache = context.getStateStore(storeName);
  }

  @Override
  public void process(FixedKeyRecord<String, BookingRequestAvro> requestRecord) {
    BookingRequestAvro request = requestRecord.value();

    List<Long> ticketIds =
        request.getSeatIds().stream()
            .map(seatId -> generateTicketId(request.getShowTimeId(), seatId))
            .toList();

    log.info("Request seatIds: {}", request.getSeatIds());
    log.info("Request showTimeId: {}", request.getShowTimeId());
    log.info("Generated ticketIds: {}", ticketIds);

    // 캐시 검증
//    if (!validateTicketsInCache(ticketIds)) {
//      return;
//    }

    BookingCommandAvro command =
        BookingCommandAvro.newBuilder()
            .setBookingId(request.getRequestId())
            .setSectionId(request.getSectionId())
            .setTicketIds(ticketIds)
            .setMemberId(request.getMemberId())
            .build();

    context.forward(requestRecord.withValue(command));
  }

  private long generateTicketId(long showTimeId, long seatId) {
    return (showTimeId << 32) | seatId;
  }

  private boolean validateTicketsInCache(List<Long> ticketIds) {
    Instant curTime = Instant.now();
    for (Long ticketId : ticketIds) {
      ValueAndTimestamp<TicketAvro> valueAndTimestamp = ticketCache.get(ticketId.toString());

      if (valueAndTimestamp == null) {
        continue;
      }

      TicketAvro ticket = valueAndTimestamp.value();

      if (!"AVAILABLE".equals(ticket.getStatus())) {
        return false;
      }

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
