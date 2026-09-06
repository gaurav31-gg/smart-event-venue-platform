package com.venuelink.venueservice.dto;

import com.venuelink.venueservice.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

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

    public static VenueResponse fromEntity(Venue venue) {
        return new VenueResponse(
                venue.getVenueId(),
                venue.getVenueName(),
                venue.getCity(),
                venue.getCapacity(),
                venue.getVenueType().name(),
                venue.getFacilities().stream()
                        .map(f -> f.getFacilityName())
                        .collect(Collectors.toList()),
                venue.getStatus().name()
        );
    }
}