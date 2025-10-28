package com.seatwise;

import com.booking.system.BookingRequestAvro;
import java.util.List;

public record BookingRequest(Long memberId, List<Long> ticketIds, Long sectionId) {

  public BookingRequestAvro toAvro(String requestId) {
    return BookingRequestAvro.newBuilder()
        .setMemberId(this.memberId)
        .setTicketIds(this.ticketIds)
        .setSectionId(this.sectionId)
        .setRequestId(requestId)
        .build();
  }
}
