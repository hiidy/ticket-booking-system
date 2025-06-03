package com.seatwise.ticket;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.seat.domain.SeatRepository;
import com.seatwise.showtime.domain.ShowTime;
import com.seatwise.showtime.domain.ShowTimeRepository;
import com.seatwise.showtime.dto.response.SeatAvailabilityResponse;
import com.seatwise.ticket.domain.Ticket;
import com.seatwise.ticket.domain.TicketRepository;
import com.seatwise.ticket.dto.ShowSeatCreateRequest;
import com.seatwise.ticket.dto.ShowSeatResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

  private final TicketRepository ticketRepository;
  private final ShowTimeRepository showTimeRepository;
  private final SeatRepository seatRepository;

  public List<Long> createShowSeat(Long showId, ShowSeatCreateRequest request) {

    ShowTime showTime =
        showTimeRepository
            .findById(showId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));

    List<Ticket> tickets =
        request.showSeatPrices().stream()
            .map(
                seatPrice ->
                    seatRepository
                        .findByIdBetween(seatPrice.startSeatId(), seatPrice.endSeatId())
                        .stream()
                        .map(seat -> Ticket.createAvailable(showTime, seat, seatPrice.price()))
                        .toList())
            .flatMap(Collection::stream)
            .toList();

    List<Ticket> savedTickets = ticketRepository.saveAll(tickets);
    return savedTickets.stream().map(Ticket::getId).toList();
  }

  public List<ShowSeatResponse> getShowSeats(Long showId) {
    List<Ticket> tickets = ticketRepository.findAllByShowTimeId(showId);
    LocalDateTime requestTime = LocalDateTime.now();
    if (tickets.isEmpty()) {
      throw new BusinessException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }
    return tickets.stream().map(showSeat -> ShowSeatResponse.from(showSeat, requestTime)).toList();
  }

  public List<SeatAvailabilityResponse> getAvailableSeatsForShow(Long showId) {
    return ticketRepository.findSeatAvailabilityByShowId(showId);
  }
}
