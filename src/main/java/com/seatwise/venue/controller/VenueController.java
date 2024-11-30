package com.seatwise.venue.controller;

import com.seatwise.venue.controller.request.VenueCreateRequest;
import com.seatwise.venue.service.VenueService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

  private final VenueService venueService;

  @PostMapping
  public ResponseEntity<Void> createVenue(@Valid @RequestBody VenueCreateRequest request) {
    Long venueId = venueService.createVenue(request.toCreateDto());
    return ResponseEntity.created(URI.create("/api/venues/" + venueId)).build();
  }
}
