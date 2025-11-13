package com.seatwise.dto;

import com.booking.system.BookingRequestAvro;
import java.util.List;

public record BookingRequest(
    Long memberId,
    Long showTimeId,
    List<Long> seatIds,
    Long sectionId
) {
  public BookingRequestAvro toAvro(String requestId) {
    return BookingRequestAvro.newBuilder()
        .setMemberId(this.memberId)
        .setShowTimeId(this.showTimeId)
        .setSeatIds(this.seatIds)
        .setSectionId(this.sectionId)
        .setRequestId(requestId)
        .build();
  }
}
