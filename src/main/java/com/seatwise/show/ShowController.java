package com.seatwise.show;

import com.seatwise.show.dto.request.ShowCreateRequest;
import com.seatwise.show.dto.response.ShowCreateResponse;
import com.seatwise.show.service.ShowService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {
  private final ShowService showService;

  @PostMapping
  public ResponseEntity<Void> createShow(@Valid @RequestBody ShowCreateRequest createRequest) {
    ShowCreateResponse createResponse = showService.createShow(createRequest);
    return ResponseEntity.created(URI.create("/api/shows/" + createResponse.id())).build();
  }
}
