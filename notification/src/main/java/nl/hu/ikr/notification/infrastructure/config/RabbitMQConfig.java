package nl.hu.ikr.notification.infrastructure.config;

@Configuration
public class RabbitMQConfig {

    @Bean
    Queue registrationQueue() {
        return new Queue("q.notification.registration");
    }

    @Bean
    Queue eventUpdatedQueue() {
        return new Queue("q.notification.event.updated");
    }

    @Bean
    Queue eventCancelledQueue() {
        return new Queue("q.notification.event.cancelled");
    }
}