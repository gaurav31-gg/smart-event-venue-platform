package com.venuelink.customerservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venuelink.customerservice.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{
	boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    Optional<Customer> findByEmail(String email);

}
