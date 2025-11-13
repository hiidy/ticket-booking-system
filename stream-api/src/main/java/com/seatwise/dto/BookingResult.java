package com.seatwise.dto;

import com.booking.system.BookingAvro;
import java.time.Instant;
import java.util.List;

public record BookingResult(
    String bookingId,
    Long memberId,
    Long sectionId,
    List<Long> ticketIds,
    Long totalAmount,
    String status,
    String errorMessage,
    Instant timestamp) {
  public static BookingResult from(BookingAvro avro) {
    return new BookingResult(
        avro.getBookingId(),
        avro.getMemberId(),
        avro.getSectionId(),
        avro.getTicketIds(),
        avro.getTotalAmount(),
        avro.getStatus().toString(),
        avro.getErrorMessage(),
        Instant.ofEpochMilli(avro.getTimestamp().toEpochMilli()));
  }
}
