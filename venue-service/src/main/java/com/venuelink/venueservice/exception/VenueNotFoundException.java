package com.venuelink.venueservice.exception;

public class VenueNotFoundException extends RuntimeException {

   
	private static final long serialVersionUID = 1L;

	public VenueNotFoundException(String message) {
        super(message);
    }
}