package com.seatwise.showtime.dto.response;

public record ShowSummaryResponse(
    Long id, String eventName, String eventType, String startTime, String date, String venue) {

  public static ShowSummaryResponse from(ShowSummaryQueryDto queryDto) {
    return new ShowSummaryResponse(
        queryDto.showId(),
        queryDto.eventTitle(),
        queryDto.showType().name(),
        queryDto.startTime().toString(),
        queryDto.date().toString(),
        queryDto.venueName());
  }
}
