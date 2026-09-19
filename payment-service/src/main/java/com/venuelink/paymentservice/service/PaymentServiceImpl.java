package com.venuelink.paymentservice.service;

import com.venuelink.paymentservice.dto.PaymentRequest;
import com.venuelink.paymentservice.dto.PaymentResponse;
import com.venuelink.paymentservice.entity.Payment;
import com.venuelink.paymentservice.exception.PaymentNotFoundException;
import com.venuelink.paymentservice.exception.PaymentProcessingException;
import com.venuelink.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {

        // --- IDEMPOTENCY CHECK ---
        // If we've already processed a payment with this exact key, return the
        // stored result instead of processing again. This is what protects us
        // from double-charging on a retried request.
        var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return PaymentResponse.fromEntity(existing.get());
        }

        // --- SIMULATED PAYMENT PROCESSING ---
        // No real payment gateway here — we simulate success/failure so the
        // rest of the system (Resilience4j fallback, booking status updates)
        // can be genuinely tested end-to-end.
        boolean success = simulatePaymentGateway();

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .transactionReference(generateTransactionReference())
                .paymentStatus(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
                .paymentDate(LocalDateTime.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (!success) {
            // We still SAVE the failed payment record (for audit/history), but we
            // throw so the caller (Booking Service) knows this attempt failed and
            // can run its compensating action (release seats, mark booking FAILED).
            throw new PaymentProcessingException(
                    "Payment failed for booking id: " + request.getBookingId());
        }

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    /**
     * Simulates a payment gateway with an 80% success rate, so we can realistically
     * test both the happy path AND the failure/resilience path without needing a
     * real payment provider integration.
     */
    private boolean simulatePaymentGateway() {
        return ThreadLocalRandom.current().nextInt(100) < 80;
    }

    private String generateTransactionReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}