package com.elton.eventplanner.event.application.dto;

import java.time.LocalDate;

public record EventResult(
        Long id,
        String name,
        LocalDate date,
        String location,
        String description,
        String status,
        Long userId
) {}
