package nl.teamrocket.identity.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.application.command.AssignRoleCommand;
import nl.teamrocket.identity.application.command.RevokeRoleCommand;
import nl.teamrocket.identity.application.event.IdentityDomainEvents;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.domain.exception.AccountNotFoundException;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.security.CorrelationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleManagementHandler {

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public void assign(AssignRoleCommand cmd) {
        Account account = accountRepository.findById(cmd.accountId())
                .orElseThrow(() -> new AccountNotFoundException(cmd.accountId()));
        account.assignRole(cmd.role());
        accountRepository.save(account);
        eventPublisher.publish("account.role-assigned.v1",
                new IdentityDomainEvents.RoleAssigned(
                        account.getId(), cmd.role().name(),
                        Instant.now(), CorrelationContext.current()));
        log.info("Role {} assigned to account: {}", cmd.role(), account.getId());
    }

    @Transactional
    public void revoke(RevokeRoleCommand cmd) {
        Account account = accountRepository.findById(cmd.accountId())
                .orElseThrow(() -> new AccountNotFoundException(cmd.accountId()));
        account.revokeRole(cmd.role());
        accountRepository.save(account);
        log.info("Role {} revoked from account: {}", cmd.role(), account.getId());
    }
}

