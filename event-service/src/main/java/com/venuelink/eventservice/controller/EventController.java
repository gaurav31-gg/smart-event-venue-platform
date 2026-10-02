package com.venuelink.eventservice.controller;

import com.venuelink.eventservice.dto.EventRequest;
import com.venuelink.eventservice.dto.EventResponse;
import com.venuelink.eventservice.exception.ForbiddenException;
import com.venuelink.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("Only ADMIN can create events");
        }

        EventResponse response = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long eventId) {

        EventResponse response = eventService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }
}