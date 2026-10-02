package com.venuelink.customerservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.venuelink.customerservice.dto.CustomerRegistrationRequest;
import com.venuelink.customerservice.dto.CustomerResponse;
import com.venuelink.customerservice.dto.LoginRequest;
import com.venuelink.customerservice.dto.LoginResponse;
import com.venuelink.customerservice.entity.Customer;
import com.venuelink.customerservice.exception.CustomerAlreadyExistsException;
import com.venuelink.customerservice.exception.CustomerNotFoundException;
import com.venuelink.customerservice.exception.InvalidCredentialsException;
import com.venuelink.customerservice.repository.CustomerRepository;
import com.venuelink.customerservice.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
                .password(passwordEncoder.encode(request.getPassword()))
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
    
    
    
    @Override
    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(
                customer.getCustomerId(),
                customer.getEmail(),
                customer.getRole().name()
        );

        return new LoginResponse(
                token,
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getRole().name()
        );
    }
}