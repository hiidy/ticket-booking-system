package com.seatwise.show.service;

import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BusinessException;
import com.seatwise.show.dto.request.TicketCreateRequest;
import com.seatwise.show.dto.response.SeatAvailabilityResponse;
import com.seatwise.show.dto.response.TicketResponse;
import com.seatwise.show.entity.Show;
import com.seatwise.show.entity.Ticket;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.repository.TicketRepository;
import com.seatwise.venue.entity.SeatRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

  private final TicketRepository ticketRepository;
  private final ShowRepository showRepository;
  private final SeatRepository seatRepository;

  public List<Long> createTickets(Long showId, TicketCreateRequest request) {
    Show show =
        showRepository
            .findById(showId)
            .orElseThrow(() -> new BusinessException(BaseCode.SHOW_NOT_FOUND));

    List<Ticket> tickets =
        request.ticketPrices().stream()
            .map(
                ticketPrice ->
                    seatRepository
                        .findByIdBetween(ticketPrice.startSeatId(), ticketPrice.endSeatId())
                        .stream()
                        .map(seat -> Ticket.createAvailable(show, seat, ticketPrice.price()))
                        .toList())
            .flatMap(Collection::stream)
            .toList();

    List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
    return savedTickets.stream().map(Ticket::getId).toList();
  }

  public List<TicketResponse> getAvailableTickets(Long showId, Long sectionId) {

    return null;
  }

  public List<TicketResponse> getTickets(Long showId) {
    List<Ticket> tickets = ticketRepository.findAllByShowId(showId);
    LocalDateTime requestTime = LocalDateTime.now();
    if (tickets.isEmpty()) {
      throw new BusinessException(BaseCode.TICKET_NOT_FOUND);
    }
    return tickets.stream().map(showSeat -> TicketResponse.from(showSeat, requestTime)).toList();
  }

  public List<SeatAvailabilityResponse> getTicketAvailabilityByGrade(Long showId) {
    return ticketRepository.findTicketAvailabilityByShowId(showId);
  }
}
