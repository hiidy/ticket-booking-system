package com.seatwise;

import com.booking.system.TicketCreateAvro;
import com.booking.system.TicketPriceRange;
import com.seatwise.dto.TicketCreateRequest;
import com.seatwise.dto.TicketCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

  private final KafkaTemplate<String, TicketCreateAvro> kafkaTemplate;
  private final KafkaTopicProperties properties;

  @PostMapping("/{showTimeId}/seats")
  public ResponseEntity<TicketCreateResponse> createTickets(
      @PathVariable Long showTimeId, @Valid @RequestBody TicketCreateRequest request) {

    TicketCreateAvro createAvro =
        TicketCreateAvro.newBuilder()
            .setShowTimeId(showTimeId)
            .setTicketPrices(
                request.ticketPrices().stream()
                    .map(
                        tp ->
                            TicketPriceRange.newBuilder()
                                .setStartSeatId(tp.startSeatId())
                                .setEndSeatId(tp.endSeatId())
                                .setPrice(tp.price())
                                .build())
                    .toList())
            .build();

    kafkaTemplate.send(properties.ticketInit(), showTimeId.toString(), createAvro);
    return ResponseEntity.ok(new TicketCreateResponse(showTimeId));
  }
}
