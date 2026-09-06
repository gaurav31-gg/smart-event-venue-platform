package com.venuelink.customerservice.service;

import com.venuelink.customerservice.dto.CustomerRegistrationRequest;
import com.venuelink.customerservice.dto.CustomerResponse;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRegistrationRequest request);
    CustomerResponse getCustomerById(Long customerId);
}