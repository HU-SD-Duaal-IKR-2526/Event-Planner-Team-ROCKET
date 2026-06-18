package nl.teamrocket.identity.adapter.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.port.outbound.EmailNotificationPort;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Secondary adapter: implements EmailNotificationPort via RabbitMQ events.
 *
 * Instead of calling SMTP directly, Identity publishes domain events to the
 * event bus. The Email BC is a downstream subscriber that handles template
 * rendering, SMTP sending, retries, and bounce handling.
 *
 * This is the correct design per the communication document §6.3:
 * "Identity / Registration / Event -> Email: Upstream via domain events"
 *
 * Routing keys:
 *   email.verification-requested.v1
 *   email.password-reset-requested.v1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqEmailEventPublisher implements EmailNotificationPort {

    private static final String EVENTS_EXCHANGE = "ep.events";

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.email.base-url:http://localhost:3000}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = baseUrl + "/verify-email?token=" + token;
        publish("email.verification-requested.v1", Map.of(
                "messageId",     UUID.randomUUID().toString(),
                "to",            toEmail,
                "templateRef",   "email-verification",
                "templateVars",  Map.of("verifyUrl", verifyUrl, "expiryHours", "24"),
                "correlationId", CorrelationContext.current(),
                "requestedAt",   Instant.now().toString()
        ));
        log.info("Published email.verification-requested for {}", toEmail);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        publish("email.password-reset-requested.v1", Map.of(
                "messageId",     UUID.randomUUID().toString(),
                "to",            toEmail,
                "templateRef",   "password-reset",
                "templateVars",  Map.of("resetUrl", resetUrl, "expiryHours", "1"),
                "correlationId", CorrelationContext.current(),
                "requestedAt",   Instant.now().toString()
        ));
        log.info("Published email.password-reset-requested for {}", toEmail);
    }

    private void publish(String routingKey, Object payload) {
        String messageId     = UUID.randomUUID().toString();
        String correlationId = CorrelationContext.current();
        rabbitTemplate.convertAndSend(EVENTS_EXCHANGE, routingKey, payload, msg -> {
            msg.getMessageProperties().setMessageId(messageId);
            msg.getMessageProperties().setCorrelationId(correlationId);
            return msg;
        });
    }
}
