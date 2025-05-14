package com.seatwise.show.dto.response;

import com.seatwise.show.domain.Show;

public record ShowSummaryResponse(
    Long id, String eventName, String eventType, String startTime, String date, String venue) {

  public static ShowSummaryResponse from(Show show) {
    return new ShowSummaryResponse(
        show.getId(),
        show.getEvent().getTitle(),
        show.getEvent().getType().name(),
        show.getDate().toString(),
        show.getStartTime().toString(),
        show.getVenue().getName());
  }
}
