package com.venuelink.customerservice.dto;

import com.venuelink.customerservice.entity.Customer;

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

    public static CustomerResponse fromEntity(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getMobileNumber(),
                customer.getCity(),
                customer.getMembershipType().name(),
                customer.getStatus().name()
        );
    }
}