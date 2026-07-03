package nl.hu.ikr.registration.infrastructure;

import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polt de outbox tabel en publiceert PENDING berichten naar RabbitMQ.
 * Bij succes wordt het bericht gemarkeerd als PUBLISHED.
 * Bij fout blijft het PENDING en wordt het opnieuw geprobeerd.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final String EXCHANGE = "registration.events";

    private final OutboxRepository outboxRepo;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxRepository outboxRepo, RabbitTemplate rabbitTemplate) {
        this.outboxRepo = outboxRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void publishPending() {
        List<OutboxMessage> pending = outboxRepo.findPending();
        for (OutboxMessage msg : pending) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, msg.getRoutingKey(), msg.getPayload());
                msg.markPublished();
                outboxRepo.save(msg);
                log.debug("Outbox bericht gepubliceerd: {} [{}]", msg.getRoutingKey(), msg.getId());
            } catch (Exception e) {
                log.warn("Kon outbox bericht niet publiceren, wordt opnieuw geprobeerd: {} — {}",
                         msg.getId(), e.getMessage());
                // Laat PENDING staan voor volgende poll
            }
        }
    }
}

