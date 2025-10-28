package com.seatwise.booking;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketAvro;
import com.seatwise.KafkaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.streams.KafkaStreamsInteractiveQueryService;

@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class BookingTopology {

  private final KafkaTopicProperties topicProperties;
  private final BookingRequestProcessor bookingRequestProcessor;
  private final Serde<String> stringSerde;
  private final Serde<BookingRequestAvro> bookingRequestSerde;
  private final Serde<BookingCommandAvro> bookingCommandAvroSerde;
  private final Serde<BookingAvro> bookingAvroSerde;
  private final Serde<TicketAvro> ticketAvroSerde;

  @Bean
  public KafkaStreamsInteractiveQueryService queryService(StreamsBuilderFactoryBean factoryBean) {
    return new KafkaStreamsInteractiveQueryService(factoryBean);
  }

  @Bean
  public KStream<String, BookingAvro> bookingStream(StreamsBuilder builder) {

    // retrieve GlobalKTable cache
    builder.globalTable(
        topicProperties.ticketState(),
        Consumed.with(Serdes.String(), ticketAvroSerde),
        Materialized.as("ticket-cache"));

    // BookingRequest Stream
    KStream<String, BookingRequestAvro> bookingRequests =
        builder.stream(
            topicProperties.bookingRequest(), Consumed.with(stringSerde, bookingRequestSerde));

    // processor
    KStream<String, BookingCommandAvro> commands =
        bookingRequests
            .processValues(
                BookingRequestProcessor::new, Named.as("booking-command-processor"))
            .filter((key, value) -> value != null);

    // ticket service로 전송
    commands.to(
        topicProperties.bookingCommand(), Produced.with(stringSerde, bookingCommandAvroSerde));

    // ticket service로부터 결과 받기
    KStream<String, BookingAvro> results = builder.stream(topicProperties.bookingResult(), Consumed.with(stringSerde, bookingAvroSerde));
    results.to(topicProperties.bookingCompleted(), Produced.with(stringSerde, bookingAvroSerde));
    return results;
  }
}
