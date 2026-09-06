package com.venuelink.venueservice.repository;

import com.venuelink.venueservice.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Optional<Facility> findByFacilityName(String facilityName);

    List<Facility> findByFacilityNameIn(List<String> facilityNames);
}