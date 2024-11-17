package com.seatwise.show;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.seatwise.event.entity.Event;
import com.seatwise.event.entity.EventType;
import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.repository.ShowRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

  @InjectMocks private ShowService showService;
  @Mock private ShowRepository showRepository;

  private Event event;

  @BeforeEach
  void setUp() {
    event =
        Event.builder()
            .title("테스트 공연")
            .description("테스트를 위한 공연입니다.")
            .type(EventType.THEATER)
            .build();
  }

  @Test
  void createShow_WithOverlappingTime_ThrowsException() {
    // Given
    Show existingShow =
        Show.builder()
            .event(event)
            .date(LocalDate.of(2024, 1, 1))
            .startTime(LocalTime.of(15, 0))
            .endTime(LocalTime.of(17, 0))
            .build();

    ShowCreateRequest showCreateRequest =
        new ShowCreateRequest(
            1L, LocalDate.of(2024, 1, 1), LocalTime.of(16, 0), LocalTime.of(17, 0));

    // When & Then
    when(showRepository.findByEventId(1L)).thenReturn(List.of(existingShow));

    // Then
    assertThatThrownBy(() -> showService.createShow(showCreateRequest))
        .isInstanceOf(DuplicateShowException.class);
  }
}
