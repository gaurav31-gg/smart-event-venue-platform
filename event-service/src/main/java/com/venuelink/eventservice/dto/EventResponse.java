package com.venuelink.eventservice.dto;

import com.venuelink.eventservice.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long eventId;
    private String eventName;
    private String eventCategory;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private Long venueId;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private String status;

    public static EventResponse fromEntity(Event event) {
        return new EventResponse(
                event.getEventId(),
                event.getEventName(),
                event.getEventCategory(),
                event.getEventDate(),
                event.getEventTime(),
                event.getVenueId(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getTicketPrice(),
                event.getStatus().name()
        );
    }
}