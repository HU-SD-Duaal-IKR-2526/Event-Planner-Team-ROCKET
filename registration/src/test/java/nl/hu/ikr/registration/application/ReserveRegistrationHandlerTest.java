package nl.hu.ikr.registration.application;

import nl.hu.ikr.registration.TestObjectMapper;
import nl.hu.ikr.registration.application.dto.RegistrationResult;
import nl.hu.ikr.registration.application.dto.ReserveRegistrationCommand;
import nl.hu.ikr.registration.domain.EventCapacity;
import nl.hu.ikr.registration.domain.OutboxMessage;
import nl.hu.ikr.registration.domain.Registration;
import nl.hu.ikr.registration.domain.RegistrationStatus;
import nl.hu.ikr.registration.repository.EventCapacityRepository;
import nl.hu.ikr.registration.repository.OutboxRepository;
import nl.hu.ikr.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReserveRegistrationHandlerTest {

    @Mock private RegistrationRepository registrationRepo;
    @Mock private EventCapacityRepository eventCapacityRepo;
    @Mock private OutboxRepository outboxRepo;

    private ReserveRegistrationHandler handler;

    private final UUID eventId = UUID.randomUUID();
    private final UUID userId  = UUID.randomUUID();

    // Herbruikbare status-lijsten
    private static final List<RegistrationStatus> RESERVED_CONFIRMED =
            List.of(RegistrationStatus.RESERVED, RegistrationStatus.CONFIRMED);
    private static final List<RegistrationStatus> WAITLISTED =
            List.of(RegistrationStatus.WAITLISTED);

    @BeforeEach
    void setup() {
        handler = new ReserveRegistrationHandler(
                registrationRepo, eventCapacityRepo, outboxRepo, TestObjectMapper.create());
    }

    // ── RESERVED pad ──────────────────────────────────────────────────────────

    @Test
    void handle_ruimteBeschikbaar_geeftRESERVED() {
        EventCapacity cap = new EventCapacity(eventId, 10, true);
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(RESERVED_CONFIRMED))).thenReturn(0L);
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResult result = handler.handle(buildCmd(0));

        assertThat(result.status()).isEqualTo(RegistrationStatus.RESERVED);
        assertThat(result.isNew()).isTrue();
        assertThat(result.position()).isNull();
    }

    @Test
    void handle_reserved_schrijftOutboxMetCorrectRoutingKey() {
        EventCapacity cap = new EventCapacity(eventId, 10, true);
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(RESERVED_CONFIRMED))).thenReturn(0L);
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(buildCmd(0));

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("registration.reserved.v1");
    }

    // ── WAITLISTED pad ────────────────────────────────────────────────────────

    @Test
    void handle_eventVol_geeftWAITLISTED() {
        EventCapacity cap = new EventCapacity(eventId, 5, true);
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(RESERVED_CONFIRMED))).thenReturn(5L);
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(WAITLISTED))).thenReturn(2L);
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResult result = handler.handle(buildCmd(0));

        assertThat(result.status()).isEqualTo(RegistrationStatus.WAITLISTED);
        assertThat(result.position()).isEqualTo(2);
    }

    @Test
    void handle_waitlisted_schrijftOutboxMetWaitlistedKey() {
        EventCapacity cap = new EventCapacity(eventId, 2, true);
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(RESERVED_CONFIRMED))).thenReturn(2L);
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(WAITLISTED))).thenReturn(0L);
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.handle(buildCmd(0));

        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("registration.waitlisted.v1");
    }

    // ── Idempotentie ──────────────────────────────────────────────────────────

    @Test
    void handle_bestaandeIdempotencyKey_returnsBestaandResultaat() {
        Registration existing = Registration.create(eventId, userId, 0, null, "WEB", "key-123");
        existing.reserve();
        when(registrationRepo.findByIdempotencyKey("key-123")).thenReturn(Optional.of(existing));

        ReserveRegistrationCommand cmd = new ReserveRegistrationCommand(
                eventId, userId, "key-123", 0, null, "WEB");
        RegistrationResult result = handler.handle(cmd);

        assertThat(result.isNew()).isFalse();
        assertThat(result.registrationId()).isEqualTo(existing.getId());
        verify(eventCapacityRepo, never()).lockByEventId(any());
    }

    // ── Foutgevallen ──────────────────────────────────────────────────────────

    @Test
    void handle_eventNietGevonden_gooit() {
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(buildCmd(0)))
                .isInstanceOf(ReserveRegistrationHandler.EventNotFoundException.class);
    }

    @Test
    void handle_eventGesloten_gooit() {
        EventCapacity cap = new EventCapacity(eventId, 10, false);
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));

        assertThatThrownBy(() -> handler.handle(buildCmd(0)))
                .isInstanceOf(ReserveRegistrationHandler.EventNotOpenException.class);
    }

    @Test
    void handle_metPlusOnes_berekentCapaciteitCorrect() {
        // Capacity 5, 4 bezet, plusOnes=1 → 4+1+1=6 > 5 → WAITLISTED
        EventCapacity cap = new EventCapacity(eventId, 5, true);
        when(eventCapacityRepo.lockByEventId(eventId)).thenReturn(Optional.of(cap));
        when(registrationRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(RESERVED_CONFIRMED))).thenReturn(4L);
        when(registrationRepo.countByEventIdAndStatusIn(eq(eventId), eq(WAITLISTED))).thenReturn(0L);
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationResult result = handler.handle(buildCmd(1));

        assertThat(result.status()).isEqualTo(RegistrationStatus.WAITLISTED);
    }

    // ── compensate ────────────────────────────────────────────────────────────

    @Test
    void compensate_annuleertRegistratie() {
        Registration reg = Registration.create(eventId, userId, 2, null, "WEB", "key");
        reg.reserve();
        when(registrationRepo.findById(reg.getId())).thenReturn(Optional.of(reg));
        when(registrationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.compensate(reg.getId());

        assertThat(reg.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepo).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("registration.cancelled.v1");
        assertThat(captor.getValue().getPayload()).contains("\"guestCount\":3");
    }

    @Test
    void compensate_registratieNietGevonden_gooit() {
        when(registrationRepo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> handler.compensate(UUID.randomUUID()))
                .isInstanceOf(ReserveRegistrationHandler.RegistrationNotFoundException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ReserveRegistrationCommand buildCmd(int plusOnes) {
        return new ReserveRegistrationCommand(
                eventId, userId, UUID.randomUUID().toString(), plusOnes, null, "WEB");
    }
}
