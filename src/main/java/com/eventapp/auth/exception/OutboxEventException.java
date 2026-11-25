package com.eventapp.auth.exception;

public class OutboxEventException extends RuntimeException {

    public OutboxEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
