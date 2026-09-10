package com.venuelink.eventservice.exception;

public class InvalidEventException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidEventException(String message) {
        super(message);
    }
}