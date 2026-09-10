package com.venuelink.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "venue-service")
public interface VenueClient {

    @GetMapping("/api/venues/{venueId}")
    VenueResponse getVenueById(@PathVariable Long venueId);
}