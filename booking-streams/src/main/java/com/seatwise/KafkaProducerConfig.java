package com.seatwise;

import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketCreateAvro;
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

  private <V> ProducerFactory<String, V> createProducerFactory(
      Serde<String> keySerde, Serde<V> valueSerde) {
    Map<String, Object> props = kafkaProperties.buildProducerProperties(null);
    props.put("schema.registry.url", schemaRegistryUrl);
    props.put("auto.register.schemas", true);

    return new DefaultKafkaProducerFactory<>(props, keySerde.serializer(), valueSerde.serializer());
  }

  @Bean
  public ProducerFactory<String, BookingRequestAvro> bookingRequestProducerFactory(
      Serde<String> stringSerde, Serde<BookingRequestAvro> bookingRequestSerde) {
    return createProducerFactory(stringSerde, bookingRequestSerde);
  }

  @Bean
  public KafkaTemplate<String, BookingRequestAvro> bookingRequestKafkaTemplate(
      ProducerFactory<String, BookingRequestAvro> bookingRequestProducerFactory) {
    return new KafkaTemplate<>(bookingRequestProducerFactory);
  }

  @Bean
  public ProducerFactory<String, TicketCreateAvro> ticketProducerFactory(
      Serde<String> stringSerde, Serde<TicketCreateAvro> ticketAvroSerde) {
    return createProducerFactory(stringSerde, ticketAvroSerde);
  }

  @Bean
  public KafkaTemplate<String, TicketCreateAvro> ticketKafkaTemplate(
      ProducerFactory<String, TicketCreateAvro> ticketProducerFactory) {
    return new KafkaTemplate<>(ticketProducerFactory);
  }
}
