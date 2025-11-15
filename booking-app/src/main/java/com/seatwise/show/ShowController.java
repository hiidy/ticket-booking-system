package com.seatwise.show;

import com.seatwise.show.dto.request.ShowRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.dto.response.ShowResponse;
import com.seatwise.show.service.ShowService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ShowController {

  private final ShowService showService;

  @PostMapping
  public ResponseEntity<Void> createEvent(@Valid @RequestBody ShowRequest showRequest) {
    ShowCreateResponse response = showService.createEvent(showRequest);
    return ResponseEntity.created(URI.create("/api/events/" + response.id())).build();
  }

  @GetMapping("/{eventId}")
  public ResponseEntity<ShowResponse> findEventById(@PathVariable Long eventId) {
    return ResponseEntity.ok(showService.findEventById(eventId));
  }
}
