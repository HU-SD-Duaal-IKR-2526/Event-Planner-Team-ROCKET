package com.elton.eventplanner.event.presentation.response;

import com.elton.eventplanner.event.application.dto.EventResult;

import java.time.LocalDate;

public record EventResponse(
        Long id,
        String name,
        LocalDate date,
        String location,
        String description,
        String status,
        Long userId
) {
    public static EventResponse from(EventResult result) {
        return new EventResponse(
                result.id(),
                result.name(),
                result.date(),
                result.location(),
                result.description(),
                result.status(),
                result.userId()
        );
    }
}
