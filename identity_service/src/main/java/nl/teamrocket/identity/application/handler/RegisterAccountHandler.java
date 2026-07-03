package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.RegisterAccountCommand;
import nl.teamrocket.identity.application.event.IdentityDomainEvents;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.application.port.outbound.EmailNotificationPort;
import nl.teamrocket.identity.domain.exception.AccountAlreadyExistsException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Email;
import nl.teamrocket.identity.domain.model.PasswordHash;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service: handles the RegisterAccount use case.
 *
 * Flow:
 *  1. Validate email uniqueness
 *  2. Hash the raw password
 *  3. Create Account aggregate (generates VerificationToken internally)
 *  4. Persist
 *  5. Send verification email
 *  6. Publish AccountRegistered domain event
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterAccountHandler {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;
    private final EmailNotificationPort emailPort;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UUID handle(RegisterAccountCommand cmd) {
        Email email = new Email(cmd.email());

        // Idempotency guard — uniqueness is also enforced by DB constraint
        if (accountRepository.existsByEmail(email)) {
            throw new AccountAlreadyExistsException(email.getValue());
        }

        String hash = passwordEncoder.encode(cmd.rawPassword());
        Account account = Account.register(email, new PasswordHash(hash));
        account = accountRepository.save(account);

        // Send verification e-mail
        String token = account.getVerificationToken().getToken();
        emailPort.sendVerificationEmail(email.getValue(), token);

        // Publish domain event
        eventPublisher.publish(
                "account.registered.v1",
                IdentityDomainEvents.AccountRegistered.from(account, CorrelationContext.current())
        );

        log.info("Account registered: {}", account.getId());
        return account.getId();
    }
}
