package com.venuelink.customerservice.service;

import com.venuelink.customerservice.dto.CustomerRegistrationRequest;
import com.venuelink.customerservice.dto.CustomerResponse;
import com.venuelink.customerservice.dto.LoginRequest;
import com.venuelink.customerservice.dto.LoginResponse;

public interface CustomerService {
    CustomerResponse registerCustomer(CustomerRegistrationRequest request);
    CustomerResponse getCustomerById(Long customerId);
    
    LoginResponse login(LoginRequest request);
}