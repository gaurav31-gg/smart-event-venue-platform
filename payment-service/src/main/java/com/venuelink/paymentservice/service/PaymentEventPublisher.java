package com.venuelink.paymentservice.service;

import com.venuelink.paymentservice.event.PaymentCompletedEvent;
import com.venuelink.paymentservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent for booking id: {}", event.getBookingId());
        kafkaTemplate.send(TOPIC, event.getBookingId().toString(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent for booking id: {}", event.getBookingId());
        kafkaTemplate.send(TOPIC, event.getBookingId().toString(), event);
    }
}