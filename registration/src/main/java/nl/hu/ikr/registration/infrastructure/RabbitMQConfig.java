package nl.hu.ikr.registration.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declareert de RabbitMQ exchanges, queues en bindings voor de Registration service.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange die deze service publiceert
    public static final String REGISTRATION_EXCHANGE = "registration.events";

    // Exchange van de Event BC (Wessel)
    public static final String EVENT_EXCHANGE = "event.events";

    // Queue voor inkomende event sync berichten
    public static final String EVENT_SYNC_QUEUE = "q.registration.event-sync";
    public static final String EVENT_SYNC_DLQ = "q.registration.event-sync.dlq";

    @Bean
    public TopicExchange registrationEventsExchange() {
        return ExchangeBuilder
                .topicExchange(REGISTRATION_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange eventEventsExchange() {
        return ExchangeBuilder
                .topicExchange(EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    // Dead-letter queue voor mislukte event-sync berichten
    @Bean
    public Queue eventSyncDlq() {
        return QueueBuilder.durable(EVENT_SYNC_DLQ).build();
    }

    @Bean
    public Queue eventSyncQueue() {
        return QueueBuilder.durable(EVENT_SYNC_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EVENT_SYNC_DLQ)
                .build();
    }

    @Bean
    public Binding eventSyncBinding(Queue eventSyncQueue, TopicExchange eventEventsExchange) {
        return BindingBuilder
                .bind(eventSyncQueue)
                .to(eventEventsExchange)
                .with("event.#");
    }
}

