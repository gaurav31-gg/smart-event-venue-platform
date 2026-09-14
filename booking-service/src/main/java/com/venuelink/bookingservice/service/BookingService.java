package com.venuelink.bookingservice.service;

import com.venuelink.bookingservice.dto.BookingRequest;
import com.venuelink.bookingservice.dto.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getBookingById(Long bookingId);
}