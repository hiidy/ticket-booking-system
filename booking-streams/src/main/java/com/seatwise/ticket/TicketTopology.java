package com.seatwise.ticket;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.TicketAvro;
import com.seatwise.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
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

@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class TicketTopology {

  private final KafkaTopicProperties topicProperties;
  private final Serde<String> stringSerde;
  private final Serde<BookingCommandAvro> bookingCommandAvroSerde;
  private final Serde<TicketAvro> ticketAvroSerde;
  private final Serde<BookingAvro> bookingAvroSerde;
  private static final String TICKET_STORE_NAME = "ticket-store";

  @Bean
  public KStream<String, BookingAvro> ticketStream(StreamsBuilder builder) {

    // Ticket Store 생성 및 등록
    KTable<String, TicketAvro> ticketTable =
        builder.table(
            topicProperties.ticketInit(),
            Consumed.with(stringSerde, ticketAvroSerde),
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

    bookingResults.to(
        topicProperties.bookingResult(), Produced.with(stringSerde, bookingAvroSerde));

    ticketTable
        .toStream()
        .to(topicProperties.ticketState(), Produced.with(stringSerde, ticketAvroSerde));

    return bookingResults;
  }
}
