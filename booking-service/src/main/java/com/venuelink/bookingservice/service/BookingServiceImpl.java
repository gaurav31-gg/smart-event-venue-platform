package com.venuelink.bookingservice.service;

import com.venuelink.bookingservice.client.*;
import com.venuelink.bookingservice.dto.BookingRequest;
import com.venuelink.bookingservice.dto.BookingResponse;
import com.venuelink.bookingservice.entity.Booking;
import com.venuelink.bookingservice.exception.*;
import com.venuelink.bookingservice.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final EventClient eventClient;
    private final CustomerClient customerClient;

    @Override
    public BookingResponse createBooking(BookingRequest request) {

        // Step 1: Validate customer exists
        validateCustomer(request.getCustomerId());

        // Step 2: Fetch event details (also validates event exists, and gives us
        // the ticket price + status needed below)
        EventResponse event = fetchEvent(request.getEventId());

        // Step 3: Block bookings on closed events (Scenario 11)
        if ("COMPLETED".equals(event.getStatus()) || "CANCELLED".equals(event.getStatus())) {
            throw new EventClosedException(
                    "Cannot book seats for an event that is " + event.getStatus());
        }

        // Step 4: Atomically reserve seats in Redis (Scenario 5 - the critical section)
        seatLockService.reserveSeats(request.getEventId(), request.getNumberOfSeats());

        // Step 5: From here on, if ANYTHING fails, we must release the seats we just
        // reserved in Redis — this is the compensating action of our saga.
        try {
            BigDecimal totalAmount = event.getTicketPrice()
                    .multiply(BigDecimal.valueOf(request.getNumberOfSeats()));

            Booking booking = Booking.builder()
                    .customerId(request.getCustomerId())
                    .eventId(request.getEventId())
                    .numberOfSeats(request.getNumberOfSeats())
                    .bookingDate(LocalDateTime.now())
                    .totalAmount(totalAmount)
                    // TEMPORARY: marking CONFIRMED directly since Payment Service
                    // doesn't exist yet. Once built, this becomes PENDING until
                    // payment succeeds.
                    .bookingStatus(Booking.BookingStatus.CONFIRMED)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            return BookingResponse.fromEntity(savedBooking);

        } catch (Exception ex) {
            // Compensating action: give back the seats since the booking didn't complete
            seatLockService.releaseSeats(request.getEventId(), request.getNumberOfSeats());
            throw ex;
        }
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking not found with id: " + bookingId));
        return BookingResponse.fromEntity(booking);
    }

    private void validateCustomer(Long customerId) {
        try {
            customerClient.getCustomerById(customerId);
        } catch (FeignException.NotFound ex) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
    }

    private EventResponse fetchEvent(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (FeignException.NotFound ex) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
    }
}