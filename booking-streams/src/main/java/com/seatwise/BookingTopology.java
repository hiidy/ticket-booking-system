package com.seatwise;

import com.booking.system.BookingAvro;
import java.util.Map;
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
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class BookingTopology {

  private final KafkaTopicProperties topicProperties;
  private final Serde<String> stringSerde;
  private final Serde<BookingAvro> bookingAvroSerde;
  private final KafkaProperties kafkaProperties;

  public static final String BOOKING_RESULT_STORE = "booking-result-store";

  @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
  public KafkaStreamsConfiguration kStreamsConfig() {
    Map<String, Object> props = kafkaProperties.buildStreamsProperties(null);
    return new KafkaStreamsConfiguration(props);
  }

  @Bean
  public KStream<String, BookingAvro> bookingStream(StreamsBuilder builder) {
    // ticket-streams로부터 결과 받기
    KStream<String, BookingAvro> results =
        builder.stream(
            topicProperties.bookingResult(), Consumed.with(stringSerde, bookingAvroSerde));

    // State Store에 저장 (Interactive Query용)
    KTable<String, BookingAvro> bookingResultTable =
        results.toTable(
            Named.as("booking-result-table"),
            Materialized.<String, BookingAvro, KeyValueStore<Bytes, byte[]>>as(BOOKING_RESULT_STORE)
                .withKeySerde(stringSerde)
                .withValueSerde(bookingAvroSerde));

    // completed 토픽으로 전송
    results.to(topicProperties.bookingCompleted(), Produced.with(stringSerde, bookingAvroSerde));
    return results;
  }
}
