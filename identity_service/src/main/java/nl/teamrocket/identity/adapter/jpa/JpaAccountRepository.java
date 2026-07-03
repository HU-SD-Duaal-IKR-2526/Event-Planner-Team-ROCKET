package nl.teamrocket.identity.adapter.jpa;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.identity.adapter.jpa.mapper.AccountMapper;
import nl.teamrocket.identity.adapter.jpa.repository.SpringDataAccountRepository;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.domain.model.Account;
import nl.teamrocket.identity.domain.model.Email;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary adapter: implements the AccountRepository port using Spring Data JPA → PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository springRepo;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        var entity = mapper.toEntity(account);
        var saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return springRepo.findByEmail(email.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByVerificationToken(String token) {
        return springRepo.findByVerificationToken(token).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByResetToken(String token) {
        return springRepo.findByResetToken(token).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springRepo.existsByEmail(email.getValue());
    }

    @Override
    public void delete(Account account) {
        springRepo.deleteById(account.getId());
    }
}
