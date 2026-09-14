package com.venuelink.bookingservice.exception;

public class InsufficientSeatException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InsufficientSeatException(String message) {
        super(message);
    }
}