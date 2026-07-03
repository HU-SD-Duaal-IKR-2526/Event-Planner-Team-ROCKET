package nl.hu.ikr.schedule.application.dto;

import nl.hu.ikr.schedule.domain.TimeSlot;

import java.util.UUID;

public record AddSessionCommand(
        UUID eventId,
        String title,
        UUID roomId,
        UUID speakerId,
        TimeSlot slot
) {}

