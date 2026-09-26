package com.venuelink.bookingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private Long bookingId;
    private Long customerId;
    private Long eventId;
    private Integer numberOfSeats;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private LocalDateTime bookingDate;
}