package nl.hu.ikr.registration.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Transactional Outbox entity.
 * Elke domein-event wordt als OutboxMessage opgeslagen in dezelfde transactie.
 * De OutboxPublisher polt deze tabel en publiceert naar RabbitMQ.
 */
@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {

    @Id
    private UUID id;

    @Column(name = "routing_key", nullable = false)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String status; // PENDING | PUBLISHED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxMessage() {}

    public static OutboxMessage pending(String routingKey, String payload) {
        OutboxMessage m = new OutboxMessage();
        m.id = UUID.randomUUID();
        m.routingKey = routingKey;
        m.payload = payload;
        m.status = "PENDING";
        m.createdAt = Instant.now();
        return m;
    }

    public void markPublished() {
        this.status = "PUBLISHED";
        this.publishedAt = Instant.now();
    }

    public UUID getId()          { return id; }
    public String getRoutingKey() { return routingKey; }
    public String getPayload()   { return payload; }
    public String getStatus()    { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
}

