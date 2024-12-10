package com.seatwise.show.dto.request;

import com.seatwise.show.dto.ShowSeatCreateDto;
import com.seatwise.show.dto.ShowSeatPrice;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ShowSeatCreateRequest(
    @NotNull Long showId, @NotNull List<ShowSeatPrice> showSeatPrices) {

  public ShowSeatCreateDto toCreateDto() {
    return new ShowSeatCreateDto(showId, showSeatPrices);
  }
}
