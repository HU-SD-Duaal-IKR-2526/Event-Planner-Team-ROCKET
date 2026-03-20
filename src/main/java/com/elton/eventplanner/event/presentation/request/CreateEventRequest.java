package com.elton.eventplanner.event.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateEventRequest(
        @NotBlank(message = "Event name cannot be blank")
        @Size(min = 4, max = 200, message = "Event name must be between 4 and 200 characters")
        String name,

        @NotNull(message = "Date cannot be null")
        LocalDate date,

        @Size(min = 4, max = 200, message = "Location must be between 4 and 200 characters")
        String location,

        @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
        String description,

        @NotNull(message = "userId cannot be null")
        Long userId
) {}
