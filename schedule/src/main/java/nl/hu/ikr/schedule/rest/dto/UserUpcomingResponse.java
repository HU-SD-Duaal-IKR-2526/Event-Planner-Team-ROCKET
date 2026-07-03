package nl.hu.ikr.schedule.rest.dto;

import java.util.List;
import java.util.UUID;

public class UserUpcomingResponse {
    private UUID userId;
    private List<UpcomingEventDto> upcoming;

    public UserUpcomingResponse() {}

    public UserUpcomingResponse(UUID userId, List<UpcomingEventDto> upcoming) {
        this.userId = userId;
        this.upcoming = upcoming;
    }

    public UUID getUserId()                   { return userId; }
    public List<UpcomingEventDto> getUpcoming() { return upcoming; }
}

