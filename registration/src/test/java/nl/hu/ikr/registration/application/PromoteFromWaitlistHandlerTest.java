package nl.hu.ikr.registration.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hu.ikr.registration.TestObjectMapper;
import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.domain.RegistrationStatus;
import nl.hu.ikr.registration.repository.OutboxRepository;
import nl.hu.ikr.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoteFromWaitlistHandlerTest {

    @Mock private RegistrationRepository registrationRepo;
    @Mock private OutboxRepository outboxRepo;

    private PromoteFromWaitlistHandler handler;

    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        handler = new PromoteFromWaitlistHandler(registrationRepo, outboxRepo, TestObjectMapper.create());
    }

    @Test
    void handle_wachtendeAanwezig_promoteertNaarRESERVED() {
        Registration waitlisted = Registration.create(
                eventId, UUID.randomUUID(), 0, null, "WEB", "key");
        waitlisted.waitlist();
        when(registrationRepo.findNextWaitlisted(eventId)).thenReturn(Optional.of(waitlisted));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(eventId);

        assertThat(waitlisted.getStatus()).isEqualTo(RegistrationStatus.RESERVED);
    }

    @Test
    void handle_wachtendeAanwezig_schrijftOutboxReserved() {
        Registration waitlisted = Registration.create(
                eventId, UUID.randomUUID(), 0, null, "WEB", "key");
        waitlisted.waitlist();
        when(registrationRepo.findNextWaitlisted(eventId)).thenReturn(Optional.of(waitlisted));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(eventId);

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("registration.reserved.v1");
    }

    @Test
    void handle_geenWachtenden_doetNiets() {
        when(registrationRepo.findNextWaitlisted(eventId)).thenReturn(Optional.empty());

        handler.handle(eventId);

        verify(registrationRepo, never()).save(any());
        verify(outboxRepo, never()).save(any());
    }
}

