package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.RequestPasswordResetCommand;
import nl.teamrocket.identity.application.event.IdentityDomainEvents;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.application.port.outbound.EmailNotificationPort;
import nl.teamrocket.identity.domain.model.Email;
import nl.teamrocket.identity.domain.model.ResetToken;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestPasswordResetHandler {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;
    private final EmailNotificationPort emailPort;

    @Transactional
    public void handle(RequestPasswordResetCommand cmd) {
        Email email = new Email(cmd.email());
        // Silently succeed even if account not found to avoid account enumeration.
        accountRepository.findByEmail(email).ifPresent(account -> {
            ResetToken token = account.initiatePasswordReset();
            accountRepository.save(account);
            emailPort.sendPasswordResetEmail(email.getValue(), token.getToken());
            eventPublisher.publish("account.password-reset-requested.v1",
                    IdentityDomainEvents.PasswordResetRequested.from(account, CorrelationContext.current()));
            log.info("Password reset requested for account: {}", account.getId());
        });
    }
}

