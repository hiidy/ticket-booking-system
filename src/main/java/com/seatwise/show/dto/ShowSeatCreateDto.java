package com.seatwise.show.dto;

import java.util.List;

public record ShowSeatCreateDto(Long showId, List<ShowSeatPrice> showSeatPrices) {}
