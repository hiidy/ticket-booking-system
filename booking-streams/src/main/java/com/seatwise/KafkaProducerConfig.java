package com.seatwise;

import com.booking.system.BookingRequestAvro;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

  private final KafkaProperties kafkaProperties;

  @Value("${spring.kafka.producer.properties.schema.registry.url}")
  private String schemaRegistryUrl;

  @Bean
  public ProducerFactory<String, BookingRequestAvro> createBookingProducerFactory(
      Serde<String> stringSerde, Serde<BookingRequestAvro> bookingRequestSerde) {
    Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.put("auto.register.schemas", true);

    return new DefaultKafkaProducerFactory<>(
        props, stringSerde.serializer(), bookingRequestSerde.serializer());
  }

  @Bean
  public KafkaTemplate<String, BookingRequestAvro> createReservationKafkaTemplate(
      ProducerFactory<String, BookingRequestAvro> bookingRequestProducerFactory) {
    return new KafkaTemplate<>(bookingRequestProducerFactory);
  }
}
