package com.venuelink.venueservice.service;

import com.venuelink.venueservice.dto.VenueRequest;
import com.venuelink.venueservice.dto.VenueResponse;

public interface VenueService {
    VenueResponse createVenue(VenueRequest request);
    VenueResponse getVenueById(Long venueId);
}