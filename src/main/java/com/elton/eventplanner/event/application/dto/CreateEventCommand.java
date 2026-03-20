package com.elton.eventplanner.event.application.dto;

import java.time.LocalDate;

public record CreateEventCommand(
        String name,
        LocalDate date,
        String location,
        String description,
        Long userId
) {}
