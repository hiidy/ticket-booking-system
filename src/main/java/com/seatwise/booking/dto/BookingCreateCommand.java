package com.seatwise.booking.dto;

import java.util.List;

public record BookingCreateCommand(Long memberId, List<Long> ticketIds, Long sectionId) {

  public static BookingCreateCommand of(Long memberId, List<Long> ticketIds, Long sectionId) {
    return new BookingCreateCommand(memberId, ticketIds, sectionId);
  }
}
