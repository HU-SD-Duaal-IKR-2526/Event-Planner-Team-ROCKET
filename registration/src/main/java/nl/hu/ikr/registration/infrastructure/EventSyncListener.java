package nl.hu.ikr.registration.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.registration.domain.EventCapacity;
import nl.hu.ikr.registration.infrastructure.dto.EventSyncMessage;
import nl.hu.ikr.registration.repository.EventCapacityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Luistert naar Event BC events om het lokale EventCapacity read-model bij te houden.
 */
@Component
public class EventSyncListener {

    private static final Logger log = LoggerFactory.getLogger(EventSyncListener.class);

    private final EventCapacityRepository eventCapacityRepo;
    private final ObjectMapper objectMapper;

    public EventSyncListener(EventCapacityRepository eventCapacityRepo, ObjectMapper objectMapper) {
        this.eventCapacityRepo = eventCapacityRepo;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "q.registration.event-sync")
    @Transactional
    public void onEventMessage(String message) {
        try {
            EventSyncMessage msg = objectMapper.readValue(message, EventSyncMessage.class);
            String type = msg.getEventType();

            if (type == null) {
                log.warn("Ontvangen event zonder eventType, genegeerd");
                return;
            }

            switch (type) {
                case "event.published" -> {
                    EventCapacity cap = new EventCapacity(msg.getEventId(), msg.getCapacity(), true);
                    eventCapacityRepo.save(cap);
                    log.info("EventCapacity aangemaakt voor event {}", msg.getEventId());
                }
                case "event.updated" -> {
                    Optional<EventCapacity> existing = eventCapacityRepo.findById(msg.getEventId());
                    if (existing.isPresent()) {
                        existing.get().setCapacity(msg.getCapacity());
                        eventCapacityRepo.save(existing.get());
                    } else {
                        // Upsert bij gemiste published
                        eventCapacityRepo.save(
                                new EventCapacity(msg.getEventId(), msg.getCapacity(), true));
                    }
                    log.info("EventCapacity bijgewerkt voor event {}", msg.getEventId());
                }
                case "event.cancelled" -> {
                    eventCapacityRepo.findById(msg.getEventId()).ifPresent(cap -> {
                        cap.setOpen(false);
                        eventCapacityRepo.save(cap);
                    });
                    log.info("Event gesloten voor inschrijvingen: {}", msg.getEventId());
                }
                default -> log.warn("Onbekend event type ontvangen: {}", type);
            }
        } catch (Exception e) {
            log.error("Fout bij verwerken van event sync bericht: {}", e.getMessage(), e);
            // Niet opnieuw gooien — anders blijft het in een retry-loop
        }
    }
}

