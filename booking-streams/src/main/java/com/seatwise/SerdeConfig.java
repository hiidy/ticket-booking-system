package com.seatwise;

import com.booking.system.BookingRequestAvro;
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

  @Value("${spring.kafka.producer.properties.schema.registry.url}")
  private String schemaRegistryUrl;

  @Bean
  public Serde<String> stringSerde() {
    return Serdes.String();
  }

  @Bean
  public Serde<BookingRequestAvro> bookingRequestSerde() {
    return avroSerde();
  }

  private <T extends SpecificRecord> Serde<T> avroSerde() {
    SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
    serde.configure(Map.of("schema.registry.url", schemaRegistryUrl), false);
    return serde;
  }
}
