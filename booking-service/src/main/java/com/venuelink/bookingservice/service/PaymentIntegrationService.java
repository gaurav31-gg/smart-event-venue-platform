package com.venuelink.bookingservice.service;

import com.venuelink.bookingservice.client.PaymentClient;
import com.venuelink.bookingservice.client.PaymentRequest;
import com.venuelink.bookingservice.client.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentIntegrationService {

    private final PaymentClient paymentClient;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public CompletableFuture<PaymentResponse> callPaymentService(PaymentRequest request) {
        return CompletableFuture.supplyAsync(
                () -> paymentClient.processPayment(request),
                executorService
        );
    }

    public CompletableFuture<PaymentResponse> paymentFallback(PaymentRequest request, Throwable throwable) {
        log.warn("Payment Service unavailable for booking id {}. Falling back. Reason: {}",
                request.getBookingId(), throwable.getMessage());

        PaymentResponse fallbackResponse = new PaymentResponse();
        fallbackResponse.setBookingId(request.getBookingId());
        fallbackResponse.setPaymentStatus("SERVICE_UNAVAILABLE");

        return CompletableFuture.completedFuture(fallbackResponse);
    }
}