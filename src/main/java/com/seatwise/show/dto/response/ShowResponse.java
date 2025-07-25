package com.seatwise.show.dto.response;

import com.seatwise.show.Show;

public record ShowResponse(Long id, String title, String description, String type) {

  public static ShowResponse from(Show show) {
    return new ShowResponse(
        show.getId(), show.getTitle(), show.getDescription(), show.getType().name());
  }
}
