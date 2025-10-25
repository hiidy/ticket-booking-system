package com.seatwise.showtime.dto.response;

public record ShowSummaryResponse(
    Long id, String title, String type, String startTime, String date, String venue) {

  public static ShowSummaryResponse from(ShowSummaryQueryDto dto) {
    return new ShowSummaryResponse(
        dto.showId(),
        dto.title(),
        dto.type().name(),
        dto.startTime().toString(),
        dto.date().toString(),
        dto.venueName());
  }
}
