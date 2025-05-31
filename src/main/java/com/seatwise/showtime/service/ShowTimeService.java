package com.seatwise.showtime.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.show.entity.Show;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.showtime.dto.request.ShowSearchCondition;
import com.seatwise.showtime.dto.request.ShowTimeCreateRequest;
import com.seatwise.showtime.dto.response.ShowDatesResponse;
import com.seatwise.showtime.dto.response.ShowSummaryQueryDto;
import com.seatwise.showtime.dto.response.ShowSummaryResponse;
import com.seatwise.showtime.dto.response.ShowTimeCreateResponse;
import com.seatwise.showtime.entity.ShowTime;
import com.seatwise.showtime.repository.ShowTimeRepository;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.repository.VenueRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowTimeService {

  private final ShowTimeRepository showTimeRepository;
  private final ShowRepository showRepository;
  private final VenueRepository venueRepository;

  public ShowTimeCreateResponse createShow(ShowTimeCreateRequest request) {
    List<ShowTime> existingShowTimes = showTimeRepository.findByShowId(request.eventId());

    Show show =
        showRepository
            .findById(request.eventId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

    Venue venue =
        venueRepository
            .findById(request.venueId())
            .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));

    ShowTime newShowTime =
        new ShowTime(show, venue, request.date(), request.startTime(), request.endTime());
    validateOverlappingShow(existingShowTimes, newShowTime);
    return ShowTimeCreateResponse.from(showTimeRepository.save(newShowTime));
  }

  private void validateOverlappingShow(List<ShowTime> existingShowTimes, ShowTime newShowTime) {
    boolean hasOverlap =
        existingShowTimes.stream()
            .anyMatch(existingShow -> existingShow.isOverlapping(newShowTime));

    if (hasOverlap) {
      throw new BusinessException(ErrorCode.DUPLICATE_SHOW);
    }
  }

  public List<ShowDatesResponse> getAvailableDates(Long eventId, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    List<ShowTime> showTimes =
        showTimeRepository.findByShowIdAndDateBetween(eventId, startDate, endDate);
    return showTimes.stream().map(ShowDatesResponse::from).toList();
  }

  public List<ShowSummaryResponse> getShows(
      ShowSearchCondition searchCondition, Pageable pageable) {
    Slice<ShowSummaryQueryDto> result =
        showTimeRepository.findShowSummaryByTypeAndDate(
            searchCondition.type(), searchCondition.date(), pageable);
    return result.getContent().stream().map(ShowSummaryResponse::from).toList();
  }
}
