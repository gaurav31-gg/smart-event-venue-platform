package com.venuelink.eventservice.service;

import com.venuelink.eventservice.client.VenueClient;
import com.venuelink.eventservice.client.VenueResponse;
import com.venuelink.eventservice.dto.EventRequest;
import com.venuelink.eventservice.dto.EventResponse;
import com.venuelink.eventservice.entity.Event;
import com.venuelink.eventservice.exception.EventNotFoundException;
import com.venuelink.eventservice.exception.InvalidEventException;
import com.venuelink.eventservice.exception.VenueNotFoundException;
import com.venuelink.eventservice.repository.EventRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueClient venueClient;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {

        VenueResponse venue = fetchVenue(request.getVenueId());

        if (request.getTotalSeats() > venue.getCapacity()) {
            throw new InvalidEventException(
                    "Total seats (" + request.getTotalSeats() +
                    ") exceeds venue capacity (" + venue.getCapacity() + ") for venue: " +
                    venue.getVenueName());
        }

        Event event = Event.builder()
                .eventName(request.getEventName())
                .eventCategory(request.getEventCategory())
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .venueId(request.getVenueId())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .ticketPrice(request.getTicketPrice())
                .build();

        Event savedEvent = eventRepository.save(event);

        return EventResponse.fromEntity(savedEvent);
    }

    @Override
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(
                        "Event not found with id: " + eventId));
        return EventResponse.fromEntity(event);
    }

    /**
     * Calls Venue Service via Feign. If Venue Service responds 404 (venue doesn't exist),
     * Feign throws a FeignException.NotFound, which we catch here and translate into
     * OUR OWN domain exception, so the rest of the codebase never has to know or care
     * that Venue Service was involved — it just sees a clean VenueNotFoundException.
     */
    private VenueResponse fetchVenue(Long venueId) {
        try {
            return venueClient.getVenueById(venueId);
        } catch (FeignException.NotFound ex) {
            throw new VenueNotFoundException("Venue not found with id: " + venueId);
        }
    }
}