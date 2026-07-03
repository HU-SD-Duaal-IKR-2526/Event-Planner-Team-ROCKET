package nl.teamrocket.core.event.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * RabbitMQ-topologie voor de Event module (alleen prod-profiel).
 *
 * De Event BC publiceert alleen; queues en bindings worden door de
 * consumerende services (registration, schedule, notification) gedeclareerd.
 * De exchange wordt hier idempotent gedeclareerd zodat publiceren ook werkt
 * als de Event service als eerste opstart.
 */
@Configuration
@Profile("prod")
public class RabbitMqConfig {

    public static final String EVENTS_EXCHANGE = "event.events";

    @Bean
    public TopicExchange eventEventsExchange() {
        return ExchangeBuilder.topicExchange(EVENTS_EXCHANGE).durable(true).build();
    }
}
