package nl.teamrocket.core.userprofile.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for User/Profile module within the Core Monolith.
 *
 * CONSUMES: account.registered.v1 (from Identity)
 * PRODUCES: profile.created.v1, profile.updated.v1, profile.avatar-updated.v1
 *
 * Binding is SPECIFIC to "account.registered.v1" — NOT "account.#" which would
 * also capture account.locked.v1 and account.login.v1 (audit-only events).
 */
@Configuration
public class RabbitMqConfig {
    public static final String EVENTS_EXCHANGE = "ep.events";
    public static final String DLX             = "ep.events.dlx";
    public static final String UP_QUEUE        = "q.core.userprofile.registered";
    public static final String UP_DLQ          = "q.core.userprofile.registered.dlq";

    @Bean TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EVENTS_EXCHANGE).durable(true).build();
    }
    @Bean TopicExchange dlx() {
        return ExchangeBuilder.topicExchange(DLX).durable(true).build();
    }
    @Bean Queue userProfileQueue() {
        return QueueBuilder.durable(UP_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-message-ttl", 60_000)
                .build();
    }
    @Bean Queue userProfileDlq() { return QueueBuilder.durable(UP_DLQ).build(); }

    // Specific binding — only account.registered.v1, not all account events
    @Bean Binding userProfileBinding() {
        return BindingBuilder.bind(userProfileQueue()).to(eventsExchange())
                .with("account.registered.v1");
    }
    @Bean Binding userProfileDlqBinding() {
        return BindingBuilder.bind(userProfileDlq()).to(dlx()).with("account.registered.v1");
    }
    @Bean Jackson2JsonMessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter c) {
        var t = new RabbitTemplate(cf);
        t.setMessageConverter(c);
        t.setMandatory(true);
        return t;
    }
}
