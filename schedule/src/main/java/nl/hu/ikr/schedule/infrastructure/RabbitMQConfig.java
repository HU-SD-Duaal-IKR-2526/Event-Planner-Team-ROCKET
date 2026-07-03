package nl.hu.ikr.schedule.infrastructure;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declareert de queues en bindings voor de Schedule service.
 * Schedule publiceert zelf niets — het is een pure consumer.
 */
@Configuration
public class RabbitMQConfig {

    public static final String REGISTRATION_EXCHANGE = "registration.events";
    public static final String EVENT_EXCHANGE = "event.events";

    public static final String SCHEDULE_REGISTRATIONS_QUEUE = "q.schedule.registrations";
    public static final String SCHEDULE_EVENTS_QUEUE        = "q.schedule.events";
    public static final String SCHEDULE_REGISTRATIONS_DLQ   = "q.schedule.registrations.dlq";
    public static final String SCHEDULE_EVENTS_DLQ          = "q.schedule.events.dlq";

    @Bean
    public TopicExchange registrationEventsExchange() {
        return ExchangeBuilder.topicExchange(REGISTRATION_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange eventEventsExchange() {
        return ExchangeBuilder.topicExchange(EVENT_EXCHANGE).durable(true).build();
    }

    // Dead-letter queues
    @Bean
    public Queue scheduleRegistrationsDlq() {
        return QueueBuilder.durable(SCHEDULE_REGISTRATIONS_DLQ).build();
    }

    @Bean
    public Queue scheduleEventsDlq() {
        return QueueBuilder.durable(SCHEDULE_EVENTS_DLQ).build();
    }

    // Werk-queues
    @Bean
    public Queue scheduleRegistrationsQueue() {
        return QueueBuilder.durable(SCHEDULE_REGISTRATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SCHEDULE_REGISTRATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue scheduleEventsQueue() {
        return QueueBuilder.durable(SCHEDULE_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SCHEDULE_EVENTS_DLQ)
                .build();
    }

    // Bindings
    @Bean
    public Binding scheduleRegistrationsBinding(Queue scheduleRegistrationsQueue,
                                                TopicExchange registrationEventsExchange) {
        return BindingBuilder
                .bind(scheduleRegistrationsQueue)
                .to(registrationEventsExchange)
                .with("registration.#");
    }

    @Bean
    public Binding scheduleEventsBinding(Queue scheduleEventsQueue,
                                         TopicExchange eventEventsExchange) {
        return BindingBuilder
                .bind(scheduleEventsQueue)
                .to(eventEventsExchange)
                .with("event.#");
    }
}

