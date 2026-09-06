package com.venuelink.venueservice.repository;

import com.venuelink.venueservice.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}