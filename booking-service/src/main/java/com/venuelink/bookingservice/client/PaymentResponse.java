package com.venuelink.bookingservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private String paymentMode;
    private String transactionReference;
    private String paymentStatus;
    private LocalDateTime paymentDate;
}