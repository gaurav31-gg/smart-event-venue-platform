package com.venuelink.bookingservice.service;

import com.venuelink.bookingservice.client.EventClient;
import com.venuelink.bookingservice.client.EventResponse;
import com.venuelink.bookingservice.exception.EventNotFoundException;
import com.venuelink.bookingservice.exception.InsufficientSeatException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> seatReservationScript;
    private final EventClient eventClient;

    private static final String SEAT_KEY_PREFIX = "event:";
    private static final String SEAT_KEY_SUFFIX = ":availableSeats";

    /**
     * Attempts to atomically reserve `numberOfSeats` for the given event.
     * Returns the remaining seat count on success.
     * Throws InsufficientSeatException if not enough seats are available.
     */
    public long reserveSeats(Long eventId, int numberOfSeats) {

        String key = buildKey(eventId);

        long result = executeScript(key, numberOfSeats);

        if (result == -2) {
            // Key doesn't exist in Redis yet — seed it from Event Service, then retry once
            seedSeatCount(eventId, key);
            result = executeScript(key, numberOfSeats);
        }

        if (result == -1) {
            throw new InsufficientSeatException(
                    "Not enough seats available for event id: " + eventId);
        }

        if (result == -2) {
            // Still -2 after seeding attempt — something went wrong seeding
            throw new IllegalStateException(
                    "Failed to initialize seat count in Redis for event id: " + eventId);
        }

        return result;
    }

    /**
     * Compensating action: give back seats if a later step in the booking flow
     * fails after we've already decremented in Redis (e.g. MySQL save fails,
     * or payment fails after booking creation).
     */
    public void releaseSeats(Long eventId, int numberOfSeats) {
        String key = buildKey(eventId);
        redisTemplate.opsForValue().increment(key, numberOfSeats);
    }

    private long executeScript(String key, int numberOfSeats) {
        Long result = redisTemplate.execute(
                seatReservationScript,
                Collections.singletonList(key),
                String.valueOf(numberOfSeats)
        );
        return result != null ? result : -2;
    }

    private void seedSeatCount(Long eventId, String key) {
        EventResponse event = fetchEvent(eventId);
        // Only set if not already set by another concurrent request (race-safe seeding)
        redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(event.getAvailableSeats()));
    }

    private EventResponse fetchEvent(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (FeignException.NotFound ex) {
            throw new EventNotFoundException("Event not found with id: " + eventId);
        }
    }

    private String buildKey(Long eventId) {
        return SEAT_KEY_PREFIX + eventId + SEAT_KEY_SUFFIX;
    }
}