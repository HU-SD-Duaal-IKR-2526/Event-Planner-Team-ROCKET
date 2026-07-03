package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.ResendVerificationCommand;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.EmailNotificationPort;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Email;
import nl.teamrocket.identity.domain.model.VerificationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResendVerificationHandler {

    private final AccountRepository accountRepository;
    private final EmailNotificationPort emailPort;

    @Transactional
    public void handle(ResendVerificationCommand cmd) {
        Email email = new Email(cmd.email());
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException(cmd.email()));
        VerificationToken token = account.regenerateVerificationToken();
        accountRepository.save(account);
        emailPort.sendVerificationEmail(email.getValue(), token.getToken());
        log.info("Verification e-mail resent for account: {}", account.getId());
    }
}

