package com.seatwise.show.dto.request;

import com.seatwise.show.dto.ShowSeatPrice;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ShowSeatCreateRequest(@NotNull @Size(min = 1) List<ShowSeatPrice> showSeatPrices) {}
