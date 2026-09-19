package com.venuelink.paymentservice.dto;

import com.venuelink.paymentservice.entity.Payment;
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

    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getPaymentMode().name(),
                payment.getTransactionReference(),
                payment.getPaymentStatus().name(),
                payment.getPaymentDate()
        );
    }
}