package nl.teamrocket.identity.application.port.outbound;

/**
 * Outbound port: trigger transactional e-mails from Identity flows.
 *
 * DESIGN DECISION (Communication doc §6.3 + Context Map):
 * Identity publishes a domain event (e.g. account.password-reset-requested.v1)
 * to RabbitMQ. The Email BC listens to those events and handles the actual SMTP send.
 *
 * Identity does NOT call SMTP directly — that would create tight coupling
 * and bypass the Email BC's retry, template, and bounce-handling logic.
 *
 * This port is implemented by the RabbitMqEmailEventPublisher adapter,
 * which publishes the appropriate domain event to the broker.
 */
public interface EmailNotificationPort {

    /**
     * Request that a verification e-mail be sent.
     * Implementation publishes "email.verification-requested.v1" event to RabbitMQ.
     */
    void sendVerificationEmail(String toEmail, String token);

    /**
     * Request that a password-reset e-mail be sent.
     * Implementation publishes "email.password-reset-requested.v1" event to RabbitMQ.
     */
    void sendPasswordResetEmail(String toEmail, String token);
}
