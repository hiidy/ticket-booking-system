package com.seatwise.show.service;

import com.seatwise.event.domain.Event;
import com.seatwise.event.exception.EventException;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.global.exception.ErrorCode;
import com.seatwise.global.exception.NotFoundException;
import com.seatwise.show.domain.Show;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.exception.DuplicateShowException;
import com.seatwise.show.repository.ShowRepository;
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

  public ShowCreateResponse createShow(ShowCreateRequest createRequest) {
    List<Show> existingShows = showRepository.findByEventId(createRequest.eventId());

    Event event =
        eventRepository
            .findById(createRequest.eventId())
            .orElseThrow(() -> new EventException(ErrorCode.EVENT_NOT_FOUND));

    Show newShow =
        Show.builder()
            .event(event)
            .date(createRequest.date())
            .startTime(createRequest.startTime())
            .endTime(createRequest.endTime())
            .build();

    validateOverlappingShow(existingShows, newShow);
    return ShowCreateResponse.from(showRepository.save(newShow));
  }

  private void validateOverlappingShow(List<Show> existingShows, Show newShow) {
    boolean hasOverlap =
        existingShows.stream().anyMatch(existingShow -> existingShow.isOverlapping(newShow));

    if (hasOverlap) {
      throw new DuplicateShowException(ErrorCode.DUPLICATE_SHOW);
    }
  }
}
