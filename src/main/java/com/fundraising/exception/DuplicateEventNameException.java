package com.fundraising.exception;

public class DuplicateEventNameException extends RuntimeException {
    public DuplicateEventNameException(String message) {
        super(message);
    }
}
