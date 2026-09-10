package com.venuelink.eventservice.service;

import com.venuelink.eventservice.dto.EventRequest;
import com.venuelink.eventservice.dto.EventResponse;

public interface EventService {
    EventResponse createEvent(EventRequest request);
    EventResponse getEventById(Long eventId);
}