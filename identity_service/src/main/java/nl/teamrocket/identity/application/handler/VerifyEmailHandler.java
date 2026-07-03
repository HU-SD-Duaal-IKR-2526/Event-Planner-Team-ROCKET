package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.VerifyEmailCommand;
import nl.teamrocket.identity.application.event.IdentityDomainEvents;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailHandler {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void handle(VerifyEmailCommand cmd) {
        Account account = accountRepository.findByVerificationToken(cmd.token())
                .orElseThrow(() -> new AccountNotFoundException("No account found for verification token"));
        account.verifyEmail(cmd.token());
        accountRepository.save(account);
        eventPublisher.publish("account.verified.v1",
                IdentityDomainEvents.AccountVerified.from(account, CorrelationContext.current()));
        log.info("Account verified: {}", account.getId());
    }
}

