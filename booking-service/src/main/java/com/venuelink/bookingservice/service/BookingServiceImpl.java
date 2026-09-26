package com.venuelink.bookingservice.service;

import com.venuelink.bookingservice.client.*;
import com.venuelink.bookingservice.dto.BookingRequest;
import com.venuelink.bookingservice.dto.BookingResponse;
import com.venuelink.bookingservice.entity.Booking;
import com.venuelink.bookingservice.event.BookingCreatedEvent;
import com.venuelink.bookingservice.exception.*;
import com.venuelink.bookingservice.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final EventClient eventClient;
    private final CustomerClient customerClient;
    private final PaymentIntegrationService paymentIntegrationService;
    
    private final BookingEventPublisher bookingEventPublisher;

    @Override
    public BookingResponse createBooking(BookingRequest request) {

        validateCustomer(request.getCustomerId());
        EventResponse event = fetchEvent(request.getEventId());

        if ("COMPLETED".equals(event.getStatus()) || "CANCELLED".equals(event.getStatus())) {
            throw new EventClosedException(
                    "Cannot book seats for an event that is " + event.getStatus());
        }

        seatLockService.reserveSeats(request.getEventId(), request.getNumberOfSeats());

        try {
            BigDecimal totalAmount = event.getTicketPrice()
                    .multiply(BigDecimal.valueOf(request.getNumberOfSeats()));

            // Save booking as PENDING first — payment hasn't happened yet
            Booking booking = Booking.builder()
                    .customerId(request.getCustomerId())
                    .eventId(request.getEventId())
                    .numberOfSeats(request.getNumberOfSeats())
                    .bookingDate(LocalDateTime.now())
                    .totalAmount(totalAmount)
                    .bookingStatus(Booking.BookingStatus.PENDING)
                    .build();

            Booking savedBooking = bookingRepository.save(booking);

            // Now attempt payment
            String paymentStatus = attemptPayment(savedBooking, totalAmount);

            if ("SUCCESS".equals(paymentStatus)) {
                savedBooking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
                bookingRepository.save(savedBooking);
                
                //publish notification via kafka
                bookingEventPublisher.publishBookingCreated(new BookingCreatedEvent(
                        savedBooking.getBookingId(),
                        savedBooking.getCustomerId(),
                        savedBooking.getEventId(),
                        savedBooking.getNumberOfSeats(),
                        savedBooking.getTotalAmount(),
                        savedBooking.getBookingStatus().name(),
                        savedBooking.getBookingDate()
                ));
            } else {
                // Covers both "FAILED" (business decline) and "SERVICE_UNAVAILABLE"
                // (circuit breaker / timeout / Payment Service down) — both need
                // the same compensating action.
                seatLockService.releaseSeats(request.getEventId(), request.getNumberOfSeats());
                savedBooking.setBookingStatus(Booking.BookingStatus.FAILED);
                bookingRepository.save(savedBooking);
                
              //publish notification via kafka
                bookingEventPublisher.publishBookingCreated(new BookingCreatedEvent(
                        savedBooking.getBookingId(),
                        savedBooking.getCustomerId(),
                        savedBooking.getEventId(),
                        savedBooking.getNumberOfSeats(),
                        savedBooking.getTotalAmount(),
                        savedBooking.getBookingStatus().name(),
                        savedBooking.getBookingDate()
                ));
            }

            return BookingResponse.fromEntity(savedBooking);

        } catch (Exception ex) {
            // Any unexpected failure after seat reservation — release seats
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

    /**
     * Calls Payment Service (via the Resilience4j-wrapped PaymentIntegrationService)
     * and returns a simple status string: "SUCCESS", "FAILED", or "SERVICE_UNAVAILABLE".
     */
    private String attemptPayment(Booking booking, BigDecimal amount) {
        PaymentRequest paymentRequest = new PaymentRequest(
                booking.getBookingId(),
                amount,
                "UPI", // TODO: take this from the booking request if you want to support multiple modes
                "BOOKING-" + booking.getBookingId(),
                booking.getCustomerId()
        );

        try {
            PaymentResponse response = paymentIntegrationService
                    .callPaymentService(paymentRequest)
                    .get(); // .get() blocks until the CompletableFuture completes

            return response.getPaymentStatus();

        } catch (ExecutionException | InterruptedException e) {
            // This catches genuine unexpected errors from the async call itself
            log.error("Unexpected error calling Payment Service for booking {}: {}",
                    booking.getBookingId(), e.getMessage());
            return "SERVICE_UNAVAILABLE";
        }
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