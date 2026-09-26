package com.venuelink.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long notificationId;

    @Column(nullable = false)
    private Long customerId;

    private Long bookingId;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus notificationStatus = NotificationStatus.SENT;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        BOOKING_CONFIRMED, BOOKING_FAILED, PAYMENT_SUCCESS, PAYMENT_FAILED, CANCELLATION, REFUND
    }

    public enum NotificationStatus {
        SENT, FAILED
    }
}