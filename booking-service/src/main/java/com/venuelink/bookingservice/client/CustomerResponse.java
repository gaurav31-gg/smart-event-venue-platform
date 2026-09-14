package com.venuelink.bookingservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long customerId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String city;
    private String membershipType;
    private String status;
}