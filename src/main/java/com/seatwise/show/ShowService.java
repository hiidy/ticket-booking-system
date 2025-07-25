package com.seatwise.show;

import com.seatwise.core.BusinessException;
import com.seatwise.core.ErrorCode;
import com.seatwise.show.dto.request.ShowRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
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
