package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.ResetPasswordCommand;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordHandler {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void handle(ResetPasswordCommand cmd) {
        Account account = accountRepository.findByResetToken(cmd.token())
                .orElseThrow(() -> new AccountNotFoundException("No account found for reset token"));
        String newHash = passwordEncoder.encode(cmd.newPassword());
        account.resetPassword(cmd.token(), new PasswordHash(newHash));
        accountRepository.save(account);
        log.info("Password reset completed for account: {}", account.getId());
    }
}

