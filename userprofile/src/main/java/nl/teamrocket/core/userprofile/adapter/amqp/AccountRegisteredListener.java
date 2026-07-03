package nl.teamrocket.core.userprofile.adapter.amqp;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.command.CreateProfileCommand;
import nl.teamrocket.core.userprofile.application.event.AccountRegisteredEvent;
import nl.teamrocket.core.userprofile.application.handler.CreateProfileHandler;
import nl.teamrocket.core.shared.CorrelationContext;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Listens to "account.registered.v1" from Identity BC.
 * Binding: specifically "account.registered.v1" — NOT the broad "account.#"
 * that would also catch lock/login events.
 *
 * Idempotent: CreateProfileHandler skips silently if profile already exists.
 */
@Slf4j @Component @RequiredArgsConstructor
public class AccountRegisteredListener {
    private final CreateProfileHandler handler;

    @RabbitListener(queues = "q.core.userprofile.registered", ackMode = "MANUAL")
    public void onAccountRegistered(
            @Payload AccountRegisteredEvent event,
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            @Header(AmqpHeaders.CORRELATION_ID) String correlationId,
            Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try (var ignored = MDC.putCloseable("correlationId", correlationId)) {
            CorrelationContext.set(correlationId);
            log.info("account.registered received for accountId={}", event.accountId());
            handler.handle(new CreateProfileCommand(event.accountId(), event.email()));
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed processing account.registered messageId={}: {}", messageId, e.getMessage(), e);
            channel.basicNack(tag, false, false); // to DLQ
        } finally {
            CorrelationContext.clear();
        }
    }
}
