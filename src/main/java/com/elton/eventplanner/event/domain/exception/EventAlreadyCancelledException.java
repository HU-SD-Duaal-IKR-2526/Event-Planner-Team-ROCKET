package com.elton.eventplanner.event.domain.exception;

public class EventAlreadyCancelledException extends RuntimeException {

    public EventAlreadyCancelledException(Long id) {
        super("Event with id " + id + " is already cancelled");
    }
}
