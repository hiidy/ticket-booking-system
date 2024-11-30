package com.seatwise.show.service;

import com.seatwise.event.domain.Event;
import com.seatwise.event.exception.EventException;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.global.exception.ErrorCode;
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

  public ShowCreateResponse createShow(ShowCreateRequest createRequest) {
    List<Show> existingShows = showRepository.findByEventId(createRequest.eventId());

    validateOverlappingShow(existingShows, createRequest);

    Event event =
        eventRepository
            .findById(createRequest.eventId())
            .orElseThrow(() -> new EventException(ErrorCode.EVENT_NOT_FOUND));

    Show show =
        Show.builder()
            .event(event)
            .date(createRequest.date())
            .startTime(createRequest.startTime())
            .endTime(createRequest.endTime())
            .build();
    return ShowCreateResponse.from(showRepository.save(show));
  }

  private void validateOverlappingShow(List<Show> existingShows, ShowCreateRequest newShow) {
    boolean hasOverlap =
        existingShows.stream()
            .filter(existingShow -> isSameDate(existingShow, newShow))
            .anyMatch(existingShow -> isTimeOverlapping(existingShow, newShow));

    if (hasOverlap) {
      throw new DuplicateShowException(ErrorCode.DUPLICATE_SHOW);
    }
  }

  private boolean isSameDate(Show existingShow, ShowCreateRequest newShow) {
    return existingShow.getDate().equals(newShow.date());
  }

  private boolean isTimeOverlapping(Show existingShow, ShowCreateRequest newShow) {
    return !(newShow.endTime().isBefore(existingShow.getStartTime())
        || newShow.startTime().isAfter(existingShow.getEndTime()));
  }
}
