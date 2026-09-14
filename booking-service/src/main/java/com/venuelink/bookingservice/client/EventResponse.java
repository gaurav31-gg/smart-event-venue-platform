package com.venuelink.bookingservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private java.math.BigDecimal ticketPrice;
    private String status;
}