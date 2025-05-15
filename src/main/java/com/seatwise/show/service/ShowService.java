package com.seatwise.show.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.event.domain.Event;
import com.seatwise.event.repository.EventRepository;
import com.seatwise.show.domain.Show;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.request.ShowSearchCondition;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowDatesResponse;
import com.seatwise.show.dto.response.ShowSummaryQueryDto;
import com.seatwise.show.dto.response.ShowSummaryResponse;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.venue.domain.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowService {

  private final ShowRepository showRepository;
  private final EventRepository eventRepository;
  private final VenueRepository venueRepository;

  public ShowCreateResponse createShow(ShowCreateRequest request) {
    List<Show> existingShows = showRepository.findByEventId(request.eventId());

    Event event =
        eventRepository
            .findById(request.eventId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

    Venue venue =
        venueRepository
            .findById(request.venueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));

    Show newShow = new Show(event, venue, request.date(), request.startTime(), request.endTime());
    validateOverlappingShow(existingShows, newShow);
    return ShowCreateResponse.from(showRepository.save(newShow));
  }

  private void validateOverlappingShow(List<Show> existingShows, Show newShow) {
    boolean hasOverlap =
        existingShows.stream().anyMatch(existingShow -> existingShow.isOverlapping(newShow));

    if (hasOverlap) {
      throw new BusinessException(ErrorCode.DUPLICATE_SHOW);
    }
  }

  public List<ShowDatesResponse> getAvailableDates(Long eventId, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    List<Show> shows = showRepository.findByEventIdAndDateBetween(eventId, startDate, endDate);
    return shows.stream().map(ShowDatesResponse::from).toList();
  }

  public List<ShowSummaryResponse> getShows(
      ShowSearchCondition searchCondition, Pageable pageable) {
    Slice<ShowSummaryQueryDto> result =
        showRepository.findShowSummaryByTypeAndDate(
            searchCondition.type(), searchCondition.date(), pageable);
    return result.getContent().stream().map(ShowSummaryResponse::from).toList();
  }
}
