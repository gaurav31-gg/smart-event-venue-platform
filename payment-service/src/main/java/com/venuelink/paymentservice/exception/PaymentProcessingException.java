package com.venuelink.paymentservice.exception;

public class PaymentProcessingException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PaymentProcessingException(String message) {
        super(message);
    }
}