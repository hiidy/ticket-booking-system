package com.seatwise.show.dto.request;

import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.ShowType;
import jakarta.validation.constraints.NotBlank;

public record ShowRequest(
    @NotBlank String title, @NotBlank String description, @NotBlank String eventType) {

  public Show toEvent() {
    return Show.builder()
        .title(title)
        .description(description)
        .type(ShowType.valueOf(eventType))
        .build();
  }
}
