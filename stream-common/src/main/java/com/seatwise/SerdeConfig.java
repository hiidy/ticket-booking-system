package com.seatwise;

import com.booking.system.BookingAvro;
import com.booking.system.BookingCommandAvro;
import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketAvro;
import com.booking.system.TicketCreateAvro;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerdeConfig {

  @Value("${spring.kafka.properties.schema.registry.url}")
  private String schemaRegistryUrl;

  @Value("${spring.kafka.properties.basic.auth.user.info}")
  private String basicAuthUserInfo;

  @Bean
  public Serde<String> stringSerde() {
    return Serdes.String();
  }

  @Bean
  public Serde<BookingRequestAvro> bookingRequestSerde() {
    return avroSerde();
  }

  @Bean
  Serde<TicketAvro> ticketAvroSerde() {
    return avroSerde();
  }

  @Bean
  Serde<BookingAvro> bookingAvroSerde() {
    return avroSerde();
  }

  @Bean
  Serde<BookingCommandAvro> bookingCommandAvroSerde() {
    return avroSerde();
  }

  @Bean
  Serde<TicketCreateAvro> ticketCreateAvroSerde() {
    return avroSerde();
  }

  private <T extends SpecificRecord> Serde<T> avroSerde() {
    SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
    serde.configure(
        Map.of(
            "schema.registry.url", schemaRegistryUrl,
            "basic.auth.credentials.source", "USER_INFO",
            "basic.auth.user.info", basicAuthUserInfo),
        false);
    return serde;
  }
}
