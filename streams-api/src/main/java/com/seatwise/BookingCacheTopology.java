package com.seatwise;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Slf4j
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class BookingCacheTopology {

  private final KafkaTopicProperties topicProperties;
  private final Serde<String> stringSerde;
  private final Serde<BookingRequestAvro> bookingRequestSerde;
  private final Serde<BookingCommandAvro> bookingCommandAvroSerde;
  private final Serde<BookingAvro> bookingAvroSerde;
  private final Serde<TicketAvro> ticketAvroSerde;

  public static final String TICKET_CACHE_STORE = "ticket-cache";
  public static final String BOOKING_RESULT_STORE = "booking-result-store";
  private static final int MAX_CACHE_SIZE = 1000;

  @Bean
  public KStream<String, BookingCommandAvro> cacheStream(StreamsBuilder builder) {
    // retrieve GlobalKTable cache
    builder.globalTable(
        topicProperties.ticketState(),
        Consumed.with(stringSerde, ticketAvroSerde),
        Materialized.<String, TicketAvro>as(Stores.lruMap(TICKET_CACHE_STORE, MAX_CACHE_SIZE))
            .withKeySerde(stringSerde)
            .withValueSerde(ticketAvroSerde));

    builder.globalTable(
        topicProperties.bookingCompleted(),
        Consumed.with(stringSerde, bookingAvroSerde),
        Materialized.<String, BookingAvro>as(Stores.persistentKeyValueStore(BOOKING_RESULT_STORE))
            .withKeySerde(stringSerde)
            .withValueSerde(bookingAvroSerde));

    // BookingRequest Stream
    KStream<String, BookingRequestAvro> bookingRequests =
        builder.stream(
            topicProperties.bookingRequest(), Consumed.with(stringSerde, bookingRequestSerde));

    // processor
    KStream<String, BookingCommandAvro> commands =
        bookingRequests
            .processValues(
                () -> new BookingRequestProcessor(TICKET_CACHE_STORE),
                Named.as("booking-command-processor"))
            .filter((key, value) -> value != null);

    // ticket service로 전송
    commands.to(
        topicProperties.bookingCommand(), Produced.with(stringSerde, bookingCommandAvroSerde));
    return commands;
  }
}
