package com.elton.eventplanner.event.application;

import com.elton.eventplanner.event.domain.events.EventCancelledDomainEvent;
import com.elton.eventplanner.event.domain.events.EventCreatedDomainEvent;
import com.elton.eventplanner.event.domain.events.EventStatusChangedDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventDomainEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EventDomainEventHandler.class);

    @EventListener
    public void on(EventCreatedDomainEvent event) {
        LOG.info("Event created: id={}, name={}", event.eventId(), event.eventName());
    }

    @EventListener
    public void on(EventCancelledDomainEvent event) {
        LOG.info("Event cancelled: id={}", event.eventId());
    }

    @EventListener
    public void on(EventStatusChangedDomainEvent event) {
        LOG.info(
                "Event status changed: id={}, {} -> {}",
                event.eventId(),
                event.oldStatus(),
                event.newStatus());
    }
}
