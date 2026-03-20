package com.elton.eventplanner.event.application.dto;

import java.time.LocalDate;

public record UpdateEventCommand(
        Long id,
        String name,
        LocalDate date,
        String location,
        String description,
        String status,
        Long userId
) {}
