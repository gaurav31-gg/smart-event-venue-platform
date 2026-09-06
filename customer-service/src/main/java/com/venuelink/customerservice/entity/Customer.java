package com.venuelink.customerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String mobileNumber;

    private String city;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MembershipType membershipType = MembershipType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    public enum MembershipType {
        REGULAR, PREMIUM, VIP
    }

    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}