package com.seatwise.show.service;

import com.seatwise.common.exception.ConflictException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.common.exception.NotFoundException;
import com.seatwise.event.domain.Event;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowService {

  private final ShowRepository showRepository;
  private final EventRepository eventRepository;

  public Show findById(Long id) {
    return showRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.SHOW_NOT_FOUND));
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

  public List<LocalDate> getAvailableDates(Long eventId, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    return showRepository.findShowDatesByEventIdAndDateBetween(eventId, startDate, endDate);
  }

  public List<ShowResponse> getShowsByDate(Long eventId, LocalDate date) {
    return showRepository.findShowsByEventIdAndDate(eventId, date).stream()
        .map(ShowResponse::from)
        .toList();
  }
}
