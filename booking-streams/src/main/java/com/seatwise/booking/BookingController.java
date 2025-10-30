package com.seatwise.booking;

import com.booking.system.BookingAvro;
import com.booking.system.BookingRequestAvro;
import com.seatwise.KafkaTopicProperties;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.streams.KafkaStreamsInteractiveQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final KafkaTemplate<String, BookingRequestAvro> kafkaTemplate;
  private final KafkaTopicProperties topicProperties;
  private final KafkaStreamsInteractiveQueryService interactiveQueryService;
  private final RestClient restClient;

  @PostMapping
  public ResponseEntity<String> createBookingRequest(@Valid @RequestBody BookingRequest request) {
    String key = request.sectionId().toString();
    String requestID = UUID.randomUUID().toString();
    BookingRequestAvro avro = request.toAvro(requestID);

    kafkaTemplate.send(topicProperties.bookingRequest(), key, avro);
    return ResponseEntity.accepted().body(requestID);
  }

  @GetMapping("/{requestId}")
  public ResponseEntity<BookingResult> getBookingResult(@PathVariable String requestId) {
    HostInfo hostInfo =
        interactiveQueryService.getKafkaStreamsApplicationHostInfo(
            BookingTopology.BOOKING_RESULT_STORE, requestId, new StringSerializer());

    HostInfo currentHost = interactiveQueryService.getCurrentKafkaStreamsApplicationHostInfo();

    if (currentHost.equals(hostInfo)) {
      BookingAvro bookingAvro = getLocalBooking(requestId);
      return bookingAvro != null
          ? ResponseEntity.ok(BookingResult.from(bookingAvro))
          : ResponseEntity.notFound().build();
    } else {
      String url =
          String.format(
              "http://%s:%d/api/bookings/local/%s", hostInfo.host(), hostInfo.port(), requestId);

      return restClient.get().uri(url).retrieve().toEntity(BookingResult.class);
    }
  }

  @GetMapping("/local/{bookingId}")
  public ResponseEntity<BookingResult> getLocalBookingEndpoint(@PathVariable String bookingId) {
    try {
      BookingAvro bookingAvro = getLocalBooking(bookingId);
      return bookingAvro != null
          ? ResponseEntity.ok(BookingResult.from(bookingAvro))
          : ResponseEntity.notFound().build();
    } catch (InvalidStateStoreException e) {
      log.warn("State store not ready yet: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
  }

  private BookingAvro getLocalBooking(String requestId) {
    ReadOnlyKeyValueStore<String, BookingAvro> store =
        interactiveQueryService.retrieveQueryableStore(
            BookingTopology.BOOKING_RESULT_STORE, QueryableStoreTypes.keyValueStore());
    return store.get(requestId);
  }
}
