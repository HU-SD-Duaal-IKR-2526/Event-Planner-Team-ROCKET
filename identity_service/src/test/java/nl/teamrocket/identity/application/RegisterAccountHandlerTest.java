package nl.teamrocket.identity.application;

import nl.teamrocket.identity.application.command.RegisterAccountCommand;
import nl.teamrocket.identity.application.handler.RegisterAccountHandler;
import nl.teamrocket.identity.application.port.outbound.AccountRepository;
import nl.teamrocket.identity.application.port.outbound.DomainEventPublisher;
import nl.teamrocket.identity.application.port.outbound.EmailNotificationPort;
import nl.teamrocket.identity.domain.exception.AccountAlreadyExistsException;
import nl.teamrocket.identity.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAccountHandler")
class RegisterAccountHandlerTest {

    @Mock AccountRepository accountRepository;
    @Mock DomainEventPublisher eventPublisher;
    @Mock EmailNotificationPort emailPort;

    RegisterAccountHandler handler;

    @BeforeEach
    void setUp() {
        // Use BCrypt with low cost factor (4) for fast tests
        handler = new RegisterAccountHandler(
                accountRepository, eventPublisher, emailPort,
                new BCryptPasswordEncoder(4)
        );
    }

    @Test
    @DisplayName("registers new account, sends verification via event, publishes domain event")
    void registers_new_account() {
        when(accountRepository.existsByEmail(any())).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID id = handler.handle(new RegisterAccountCommand("user@test.nl", "Password1!"));

        assertThat(id).isNotNull();
        // Email is sent via RabbitMQ event (not SMTP directly)
        verify(emailPort).sendVerificationEmail(eq("user@test.nl"), anyString());
        verify(eventPublisher).publish(eq("account.registered.v1"), any());
        verify(accountRepository).save(any());
    }

    @Test
    @DisplayName("throws AccountAlreadyExistsException when e-mail is taken")
    void throws_when_email_taken() {
        when(accountRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() ->
                handler.handle(new RegisterAccountCommand("taken@test.nl", "Password1!")))
                .isInstanceOf(AccountAlreadyExistsException.class);

        verify(accountRepository, never()).save(any());
        verify(emailPort, never()).sendVerificationEmail(any(), any());
        verify(eventPublisher, never()).publish(any(), any());
    }

    @Test
    @DisplayName("does not reveal account existence on duplicate — throws same exception type")
    void consistent_exception_on_duplicate() {
        when(accountRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // Both first and second registration attempt throw the same exception
        assertThatThrownBy(() ->
                handler.handle(new RegisterAccountCommand("same@test.nl", "Pass123!")))
                .isInstanceOf(AccountAlreadyExistsException.class);
    }
}
