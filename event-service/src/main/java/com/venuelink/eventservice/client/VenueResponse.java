package com.venuelink.eventservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private Long venueId;
    private String venueName;
    private String city;
    private Integer capacity;
    private String venueType;
    private List<String> facilities;
    private String status;
}