package com.seatwise.booking;

import com.booking.system.BookingRequestAvro;
import com.seatwise.KafkaTopicProperties;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final KafkaTemplate<String, BookingRequestAvro> kafkaTemplate;
  private final KafkaTopicProperties topicProperties;

  @PostMapping
  public ResponseEntity<String> createBookingRequest(@Valid @RequestBody BookingRequest request) {
    String key = request.sectionId().toString();
    String requestID = UUID.randomUUID().toString();
    BookingRequestAvro avro = request.toAvro(requestID);

    kafkaTemplate.send(topicProperties.bookingRequest(), key, avro);
    return ResponseEntity.accepted().body(requestID);
  }
}
