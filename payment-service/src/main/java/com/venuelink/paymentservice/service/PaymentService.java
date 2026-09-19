package com.venuelink.paymentservice.service;

import com.venuelink.paymentservice.dto.PaymentRequest;
import com.venuelink.paymentservice.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse getPaymentById(Long paymentId);
}