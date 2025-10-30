package com.seatwise;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.TicketAvro;
import com.booking.system.TicketCreateAvro;
import com.booking.system.TicketPriceRange;
import com.booking.system.TicketStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Slf4j
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class TicketTopology {

  private final KafkaTopicProperties topicProperties;
  private final Serde<String> stringSerde;
  private final Serde<BookingCommandAvro> bookingCommandAvroSerde;
  private final Serde<TicketAvro> ticketAvroSerde;
  private final Serde<BookingAvro> bookingAvroSerde;
  private final Serde<TicketCreateAvro> ticketCreateAvroSerde;
  private static final String TICKET_STORE_NAME = "ticket-store";

  @Bean
  public KStream<String, BookingAvro> ticketStream(StreamsBuilder builder) {
    KStream<String, TicketCreateAvro> ticketCreateStream =
        builder.stream(
            topicProperties.ticketInit(), Consumed.with(stringSerde, ticketCreateAvroSerde));

    KStream<String, TicketAvro> expandedTickets =
        ticketCreateStream.flatMapValues(
            (key, ticketCreate) -> {
              List<TicketAvro> tickets = new ArrayList<>();

              for (TicketPriceRange priceRange : ticketCreate.getTicketPrices()) {
                long startSeatId = priceRange.getStartSeatId();
                long endSeatId = priceRange.getEndSeatId();
                long price = priceRange.getPrice();

                for (long seatId = startSeatId; seatId <= endSeatId; seatId++) {
                  TicketAvro ticket =
                      TicketAvro.newBuilder()
                          .setId(generateTicketId(ticketCreate.getShowTimeId(), seatId))
                          .setShowTimeId(ticketCreate.getShowTimeId())
                          .setSeatId(seatId)
                          .setBookingId(null)
                          .setPrice(price)
                          .setStatus(TicketStatus.AVAILABLE)
                          .setExpirationTime(null)
                          .build();
                  tickets.add(ticket);
                }
              }
              return tickets;
            });

    KStream<String, TicketAvro> ticketsWithKey =
        expandedTickets.selectKey((key, ticket) -> String.valueOf(ticket.getId()));

    KTable<String, TicketAvro> ticketTable =
        ticketsWithKey.toTable(
            Named.as("ticket-table"),
            Materialized.<String, TicketAvro, KeyValueStore<Bytes, byte[]>>as(TICKET_STORE_NAME)
                .withKeySerde(stringSerde)
                .withValueSerde(ticketAvroSerde));

    // booking process
    KStream<String, BookingCommandAvro> commands =
        builder.stream(
            topicProperties.bookingCommand(), Consumed.with(stringSerde, bookingCommandAvroSerde));

    KStream<String, BookingAvro> bookingResults =
        commands.process(
            () -> new TicketBookingProcessor(TICKET_STORE_NAME),
            Named.as("ticket-booking-processor"),
            TICKET_STORE_NAME);

    bookingResults
        .selectKey((key, booking) -> booking.getBookingId())
        .to(topicProperties.bookingResult(), Produced.with(stringSerde, bookingAvroSerde));

    ticketTable
        .toStream()
        .to(topicProperties.ticketState(), Produced.with(stringSerde, ticketAvroSerde));

    return bookingResults;
  }

  private long generateTicketId(long showTimeId, long seatId) {
    return (showTimeId << 32) | seatId;
  }
}
