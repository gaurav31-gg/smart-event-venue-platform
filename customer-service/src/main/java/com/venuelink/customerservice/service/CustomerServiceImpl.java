package com.venuelink.customerservice.service;

import com.venuelink.customerservice.dto.CustomerRegistrationRequest;
import com.venuelink.customerservice.dto.CustomerResponse;
import com.venuelink.customerservice.entity.Customer;
import com.venuelink.customerservice.exception.CustomerAlreadyExistsException;
import com.venuelink.customerservice.exception.CustomerNotFoundException;
import com.venuelink.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException(
                    "Customer already exists with email: " + request.getEmail());
        }

        if (customerRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new CustomerAlreadyExistsException(
                    "Customer already exists with mobile number: " + request.getMobileNumber());
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .city(request.getCity())
                .build();
        
        // membershipType and status use their @Builder.Default values (REGULAR, ACTIVE)

        Customer savedCustomer = customerRepository.save(customer);

        return CustomerResponse.fromEntity(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId));
        return CustomerResponse.fromEntity(customer);
    }
}