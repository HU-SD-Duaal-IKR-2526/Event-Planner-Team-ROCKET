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
class ConfirmRegistrationHandlerTest {

    @Mock private RegistrationRepository registrationRepo;
    @Mock private OutboxRepository outboxRepo;

    private ConfirmRegistrationHandler handler;

    @BeforeEach
    void setup() {
        handler = new ConfirmRegistrationHandler(registrationRepo, outboxRepo, TestObjectMapper.create());
    }

    @Test
    void handle_vanafRESERVED_zettOpCONFIRMED() {
        Registration reg = Registration.create(
                UUID.randomUUID(), UUID.randomUUID(), 1, null, "WEB", "key");
        reg.reserve();
        when(registrationRepo.findById(reg.getId())).thenReturn(Optional.of(reg));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(reg.getId());

        assertThat(reg.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
    }

    @Test
    void handle_schrijftOutboxMetConfirmedKey() {
        Registration reg = Registration.create(
                UUID.randomUUID(), UUID.randomUUID(), 0, null, "MOBILE", "key");
        reg.reserve();
        when(registrationRepo.findById(reg.getId())).thenReturn(Optional.of(reg));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(reg.getId());

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("registration.confirmed.v1");
    }

    @Test
    void handle_payloadBevatGuestCount() throws Exception {
        Registration reg = Registration.create(
                UUID.randomUUID(), UUID.randomUUID(), 2, null, "WEB", "key"); // plusOnes=2 → guestCount=3
        reg.reserve();
        when(registrationRepo.findById(reg.getId())).thenReturn(Optional.of(reg));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(reg.getId());

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        String payload = captor.getValue().getPayload();
        assertThat(payload).contains("\"guestCount\":3");
    }

    @Test
    void handle_vanafREQUESTED_gooit() {
        Registration reg = Registration.create(
                UUID.randomUUID(), UUID.randomUUID(), 0, null, "WEB", "key");
        // status = REQUESTED, niet RESERVED
        when(registrationRepo.findById(reg.getId())).thenReturn(Optional.of(reg));

        assertThatThrownBy(() -> handler.handle(reg.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REQUESTED");
    }

    @Test
    void handle_registratieNietGevonden_gooit() {
        when(registrationRepo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> handler.handle(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

