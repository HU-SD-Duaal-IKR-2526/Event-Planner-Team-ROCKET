package nl.hu.ikr.notification.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declareert de queues en bindings voor de Notification service.
 *
 * CONSUMES:
 *   registration.confirmed.v1  (exchange: registration.events, van Registration BC)
 *   event.updated.v1           (exchange: event.events, van Event BC)
 *   event.cancelled.v1         (exchange: event.events, van Event BC)
 *
 * De exchanges worden hier ook gedeclareerd (idempotent) zodat de service
 * onafhankelijk van de opstartvolgorde van de andere services kan starten.
 */
@Configuration
public class RabbitConfiguration {

    public static final String REGISTRATION_EXCHANGE = "registration.events";
    public static final String EVENT_EXCHANGE = "event.events";

    public static final String REGISTRATION_QUEUE = "q.notification.registration";
    public static final String EVENT_UPDATED_QUEUE = "q.notification.event.updated";
    public static final String EVENT_CANCELLED_QUEUE = "q.notification.event.cancelled";

    @Bean
    public TopicExchange registrationEventsExchange() {
        return ExchangeBuilder.topicExchange(REGISTRATION_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange eventEventsExchange() {
        return ExchangeBuilder.topicExchange(EVENT_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(REGISTRATION_QUEUE).build();
    }

    @Bean
    public Queue eventUpdatedQueue() {
        return QueueBuilder.durable(EVENT_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue eventCancelledQueue() {
        return QueueBuilder.durable(EVENT_CANCELLED_QUEUE).build();
    }

    @Bean
    public Binding registrationBinding(Queue registrationQueue,
                                       TopicExchange registrationEventsExchange) {
        return BindingBuilder.bind(registrationQueue)
                .to(registrationEventsExchange)
                .with("registration.confirmed.#");
    }

    @Bean
    public Binding eventUpdatedBinding(Queue eventUpdatedQueue,
                                       TopicExchange eventEventsExchange) {
        return BindingBuilder.bind(eventUpdatedQueue)
                .to(eventEventsExchange)
                .with("event.updated.#");
    }

    @Bean
    public Binding eventCancelledBinding(Queue eventCancelledQueue,
                                         TopicExchange eventEventsExchange) {
        return BindingBuilder.bind(eventCancelledQueue)
                .to(eventEventsExchange)
                .with("event.cancelled.#");
    }
}
