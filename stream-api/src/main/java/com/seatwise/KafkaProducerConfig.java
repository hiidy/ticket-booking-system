package com.seatwise;

import com.booking.system.BookingRequestAvro;
import com.booking.system.TicketCreateAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerConfig {

  private final KafkaProperties kafkaProperties;

  @Bean
  public ProducerFactory<String, BookingRequestAvro> bookingRequestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(
        kafkaProperties.buildProducerProperties(null)
    );
  }

  @Bean
  public KafkaTemplate<String, BookingRequestAvro> bookingRequestKafkaTemplate(
      ProducerFactory<String, BookingRequestAvro> bookingRequestProducerFactory) {
    return new KafkaTemplate<>(bookingRequestProducerFactory);
  }

  @Bean
  public ProducerFactory<String, TicketCreateAvro> ticketProducerFactory() {
    return new DefaultKafkaProducerFactory<>(
        kafkaProperties.buildProducerProperties(null)
    );
  }

  @Bean
  public KafkaTemplate<String, TicketCreateAvro> ticketKafkaTemplate(
      ProducerFactory<String, TicketCreateAvro> ticketProducerFactory) {
    return new KafkaTemplate<>(ticketProducerFactory);
  }
}
