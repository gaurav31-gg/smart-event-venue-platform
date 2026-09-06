package com.venuelink.venueservice.controller;

import com.venuelink.venueservice.dto.VenueRequest;
import com.venuelink.venueservice.dto.VenueResponse;
import com.venuelink.venueservice.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(
            @Valid @RequestBody VenueRequest request) {

        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> getVenueById(
            @PathVariable Long venueId) {

        VenueResponse response = venueService.getVenueById(venueId);
        return ResponseEntity.ok(response);
    }
}