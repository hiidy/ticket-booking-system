package com.seatwise;

import com.booking.system.BookingRequestAvro;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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
  public CompletableFuture<ResponseEntity<String>> createBookingRequest(
      @Valid @RequestBody BookingRequest request) {
    String key = request.sectionId().toString();
    String requestID = UUID.randomUUID().toString();
    BookingRequestAvro avro = request.toAvro(requestID);

    CompletableFuture<SendResult<String, BookingRequestAvro>> future =
        kafkaTemplate.send(topicProperties.bookingRequest(), key, avro);

    return future
        .thenApply(
            result -> {
              return ResponseEntity.accepted().body(requestID);
            })
        .exceptionally(
            ex -> {
              return ResponseEntity.internalServerError().body(null);
            });
  }
}
