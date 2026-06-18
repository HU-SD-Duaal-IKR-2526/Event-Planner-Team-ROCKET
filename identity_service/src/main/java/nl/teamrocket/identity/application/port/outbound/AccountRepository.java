package nl.teamrocket.identity.application.port.outbound;

import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Email;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence abstraction for Account aggregate.
 * The domain knows about this interface; only the JPA adapter implements it.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByEmail(Email email);

    Optional<Account> findByVerificationToken(String token);

    Optional<Account> findByResetToken(String token);

    boolean existsByEmail(Email email);

    void delete(Account account);
}
