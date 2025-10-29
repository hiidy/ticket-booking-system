package com.seatwise.ticket;

import com.booking.system.TicketAvro;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;

@RequiredArgsConstructor
public class TicketStoreProcessor implements FixedKeyProcessor<String, TicketAvro, Void> {

  private final String storeName;
  private KeyValueStore<String, TicketAvro> ticketStore;

  @Override
  public void init(FixedKeyProcessorContext<String, Void> context) {
    this.ticketStore = context.getStateStore(storeName);
  }

  @Override
  public void process(FixedKeyRecord<String, TicketAvro> record) {
    String sectionId = record.key();
    TicketAvro ticket = record.value();

    if (sectionId != null && ticket != null) {
      ticketStore.put(sectionId, ticket);
    }
  }

  @Override
  public void close() {
    FixedKeyProcessor.super.close();
  }
}
