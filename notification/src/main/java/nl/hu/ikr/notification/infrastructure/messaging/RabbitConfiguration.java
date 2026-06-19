package nl.hu.ikr.notification.infrastructure.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    public Queue registrationQueue() {
        return new Queue("q.notification.registration");
    }

    @Bean
    public Queue eventUpdatedQueue() {
        return new Queue("q.notification.event.updated");
    }

    @Bean
    public Queue eventCancelledQueue() {
        return new Queue("q.notification.event.cancelled");
    }
}