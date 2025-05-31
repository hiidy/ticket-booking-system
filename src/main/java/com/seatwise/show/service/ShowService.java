package com.seatwise.show.service;

import com.seatwise.common.exception.BusinessException;
import com.seatwise.common.exception.ErrorCode;
import com.seatwise.show.dto.request.ShowRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.entity.Show;
import com.seatwise.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowService {

  private final ShowRepository showRepository;

  public ShowCreateResponse createEvent(ShowRequest showRequest) {
    Show show = showRepository.save(showRequest.toEvent());
    return ShowCreateResponse.from(show);
  }

  public ShowResponse findEventById(Long eventId) {
    return showRepository
        .findById(eventId)
        .map(ShowResponse::from)
        .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
  }
}
