package com.seatwise.show.service;

import com.seatwise.core.BusinessException;
import com.seatwise.core.BaseCode;
import com.seatwise.show.entity.Show;
import com.seatwise.show.repository.ShowRepository;
import com.seatwise.show.entity.ShowTime;
import com.seatwise.show.repository.ShowTimeRepository;
import com.seatwise.show.dto.request.ShowSearchCondition;
import com.seatwise.show.dto.request.ShowTimeCreateRequest;
import com.seatwise.show.dto.response.ShowSummaryResponse;
import com.seatwise.show.dto.response.ShowTimeCreateResponse;
import com.seatwise.show.dto.response.ShowTimeSummaryResponse;
import com.seatwise.venue.entity.Venue;
import com.seatwise.venue.entity.VenueRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowTimeService {

  private final ShowTimeRepository showTimeRepository;
  private final ShowRepository showRepository;
  private final VenueRepository venueRepository;

  public ShowTimeCreateResponse createShowTime(ShowTimeCreateRequest request) {
    List<ShowTime> existingShowTimes = showTimeRepository.findByShowId(request.showId());

    Show show =
        showRepository
            .findById(request.showId())
            .orElseThrow(() -> new BusinessException(BaseCode.EVENT_NOT_FOUND));

    Venue venue =
        venueRepository
            .findById(request.venueId())
            .orElseThrow(() -> new BusinessException(BaseCode.VENUE_NOT_FOUND));

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
      throw new BusinessException(BaseCode.DUPLICATE_SHOW);
    }
  }

  public List<ShowTimeSummaryResponse> getAvailableDates(Long showTimeId, int year, int month) {
    LocalDate startDate = LocalDate.of(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    List<ShowTime> showTimes =
        showTimeRepository.findByShowIdAndDateGreaterThanEqualAndDateLessThan(
            showTimeId, startDate, endDate);
    return showTimes.stream().map(ShowTimeSummaryResponse::from).toList();
  }

  public Slice<ShowSummaryResponse> searchShowTimes(
      ShowSearchCondition searchCondition, Pageable pageable) {
    return showTimeRepository
        .findUpcomingShowTimes(
            searchCondition.type(),
            searchCondition.startDate(),
            searchCondition.endDate(),
            pageable)
        .map(ShowSummaryResponse::from);
  }
}
