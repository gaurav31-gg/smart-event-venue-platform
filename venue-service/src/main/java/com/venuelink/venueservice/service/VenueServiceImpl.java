package com.venuelink.venueservice.service;

import com.venuelink.venueservice.dto.VenueRequest;
import com.venuelink.venueservice.dto.VenueResponse;
import com.venuelink.venueservice.entity.Facility;
import com.venuelink.venueservice.entity.Venue;
import com.venuelink.venueservice.exception.VenueNotFoundException;
import com.venuelink.venueservice.repository.FacilityRepository;
import com.venuelink.venueservice.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final FacilityRepository facilityRepository;

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {

        Set<Facility> facilities = resolveFacilities(request.getFacilities());

        Venue venue = Venue.builder()
                .venueName(request.getVenueName())
                .city(request.getCity())
                .capacity(request.getCapacity())
                .venueType(request.getVenueType())
                .facilities(facilities)
                .build();

        Venue savedVenue = venueRepository.save(venue);

        return VenueResponse.fromEntity(savedVenue);
    }

    @Override
    public VenueResponse getVenueById(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(
                        "Venue not found with id: " + venueId));
        return VenueResponse.fromEntity(venue);
    }

    /**
     * For each facility name in the request:
     * - if it already exists in the DB, reuse it
     * - if not, create a new Facility entity
     * This avoids duplicate facility rows (e.g. two venues both having "WiFi"
     * should reference the SAME Facility row, not two separate ones).
     */
    
    private Set<Facility> resolveFacilities(List<String> facilityNames) {
        if (facilityNames == null || facilityNames.isEmpty()) {
            return new HashSet<>();
        }

        List<Facility> existingFacilities = facilityRepository.findByFacilityNameIn(facilityNames);

        Set<String> existingNames = existingFacilities.stream()
                .map(Facility::getFacilityName)
                .collect(Collectors.toSet());

        Set<Facility> newFacilities = facilityNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> {
                    Facility f = new Facility();
                    f.setFacilityName(name);
                    return f;
                })
                .collect(Collectors.toSet());

        Set<Facility> allFacilities = new HashSet<>(existingFacilities);
        allFacilities.addAll(newFacilities);
        facilityRepository.saveAll(newFacilities);

        return allFacilities;
    }
}