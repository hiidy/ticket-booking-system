package com.seatwise.ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TicketCreateRequest(@NotNull @Size(min = 1) List<TicketPrice> ticketPrices) {}
