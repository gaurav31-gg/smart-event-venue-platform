package com.venuelink.notificationservice.listener;

import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.venuelink.notificationservice.entity.Notification;
import com.venuelink.notificationservice.event.BookingCreatedEvent;
import com.venuelink.notificationservice.event.PaymentCompletedEvent;
import com.venuelink.notificationservice.event.PaymentFailedEvent;
import com.venuelink.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent for booking id: {}", event.getBookingId());

        Notification.NotificationType type = "CONFIRMED".equals(event.getBookingStatus())
                ? Notification.NotificationType.BOOKING_CONFIRMED
                : Notification.NotificationType.BOOKING_FAILED;

        String message = "CONFIRMED".equals(event.getBookingStatus())
                ? "Your booking #" + event.getBookingId() + " for " + event.getNumberOfSeats()
                    + " seat(s) has been confirmed. Total amount: " + event.getTotalAmount()
                : "Your booking #" + event.getBookingId() + " could not be completed. Please try again.";

        saveNotification(event.getCustomerId(), event.getBookingId(), message, type);
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvent(ConsumerRecord<String, Object> record) {
    	    log.info("========== PAYMENT EVENT ==========");
    	  
    	    Object event = record.value();
    	    
        if (event instanceof PaymentCompletedEvent completedEvent) {
            log.info("Received PaymentCompletedEvent for booking id: {}", completedEvent.getBookingId());

            String message = "Payment of " + completedEvent.getAmount()
                    + " successful for booking #" + completedEvent.getBookingId()
                    + ". Transaction reference: " + completedEvent.getTransactionReference();

            // Note: we don't have customerId directly in PaymentCompletedEvent —
            // in a real system we'd either include it in the event payload, or
            // look it up. For now, we log it against bookingId only.
            saveNotification(completedEvent.getCustomerId(), completedEvent.getBookingId(), message,
                    Notification.NotificationType.PAYMENT_SUCCESS);

        } else if (event instanceof PaymentFailedEvent failedEvent) {
            log.info("Received PaymentFailedEvent for booking id: {}", failedEvent.getBookingId());

            String message = "Payment of " + failedEvent.getAmount()
                    + " failed for booking #" + failedEvent.getBookingId()
                    + ". Reason: " + failedEvent.getReason();

            saveNotification(failedEvent.getCustomerId(), failedEvent.getBookingId(), message,
                    Notification.NotificationType.PAYMENT_FAILED);
        }
    }

    private void saveNotification(Long customerId, Long bookingId, String message,
                                    Notification.NotificationType type) {
        Notification notification = Notification.builder()
                .customerId(customerId)
                .bookingId(bookingId)
                .message(message)
                .notificationType(type)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notification saved: {}", message);
    }
}