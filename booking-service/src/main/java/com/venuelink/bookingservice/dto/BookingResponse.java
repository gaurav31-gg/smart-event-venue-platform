package com.venuelink.bookingservice.dto;

import com.venuelink.bookingservice.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private Long customerId;
    private Long eventId;
    private Integer numberOfSeats;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String bookingStatus;

    public static BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getEventId(),
                booking.getNumberOfSeats(),
                booking.getBookingDate(),
                booking.getTotalAmount(),
                booking.getBookingStatus().name()
        );
    }
}