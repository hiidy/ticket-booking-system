package com.seatwise.show.service;

import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.event.domain.Event;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.seat.domain.Seat;
import com.seatwise.seat.repository.SeatRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.domain.Status;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.response.SeatRemainingResponse;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowDatesResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowService {

  private final ShowRepository showRepository;
  private final EventRepository eventRepository;
  private final SeatRepository seatRepository;

  public ShowResponse getShowDetails(Long showId) {
    LocalTime startTime =
        showRepository
            .findById(showId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.SHOW_NOT_FOUND))
            .getStartTime();

    List<Seat> seats = seatRepository.findByShowIdAndStatus(showId, Status.AVAILABLE);
    if (seats.isEmpty()) {
      throw new NotFoundException(ErrorCode.SHOW_SEAT_NOT_FOUND);
    }

    List<SeatRemainingResponse> responses =
        seats.stream()
            .collect(
                Collectors.groupingBy(
                    Seat::getGrade,
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)))
            .entrySet()
            .stream()
            .map(
                entry ->
                    new SeatRemainingResponse(entry.getKey().getDescription(), entry.getValue()))
            .toList();

    return new ShowResponse(startTime, responses);
  }

  public ShowCreateResponse createShow(ShowCreateRequest request) {
    List<Show> existingShows = showRepository.findByEventId(request.eventId());

    Event event =
        eventRepository
            .findById(request.eventId())
            .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND));
    Show newShow = new Show(event, request.date(), request.startTime(), request.endTime());

    validateOverlappingShow(existingShows, newShow);
    return ShowCreateResponse.from(showRepository.save(newShow));
  }

  private void validateOverlappingShow(List<Show> existingShows, Show newShow) {
    boolean hasOverlap =
        existingShows.stream().anyMatch(existingShow -> existingShow.isOverlapping(newShow));

    if (hasOverlap) {
      throw new ConflictException(ErrorCode.DUPLICATE_SHOW);
    }
  }

  @Transactional(readOnly = true)
  public List<ShowDatesResponse> getAvailableDates(Long eventId, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    List<Show> shows = showRepository.findByEventIdAndDateBetween(eventId, startDate, endDate);
    return shows.stream().map(ShowDatesResponse::from).toList();
  }
}
