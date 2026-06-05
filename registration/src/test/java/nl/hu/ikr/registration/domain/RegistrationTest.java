package nl.hu.ikr.registration.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class RegistrationTest {

    private Registration makeRegistration() {
        return Registration.create(
                UUID.randomUUID(), UUID.randomUUID(),
                0, null, "WEB", UUID.randomUUID().toString());
    }

    @Test
    void nieuwRegistratie_heeftStatusREQUESTED() {
        Registration r = makeRegistration();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.REQUESTED);
    }

    @Test
    void reserve_vanafREQUESTED_zettStatusOpRESERVED() {
        Registration r = makeRegistration();
        r.reserve();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.RESERVED);
    }

    @Test
    void confirm_vanafRESERVED_zettStatusOpCONFIRMED() {
        Registration r = makeRegistration();
        r.reserve();
        r.confirm();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
    }

    @Test
    void waitlist_vanafREQUESTED_zettStatusOpWAITLISTED() {
        Registration r = makeRegistration();
        r.waitlist();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.WAITLISTED);
    }

    @Test
    void cancel_vanafRESERVED_zettStatusOpCANCELLED() {
        Registration r = makeRegistration();
        r.reserve();
        r.cancel();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
    }

    @Test
    void cancel_vanafCONFIRMED_zettStatusOpCANCELLED() {
        Registration r = makeRegistration();
        r.reserve();
        r.confirm();
        r.cancel();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
    }

    @Test
    void cancel_isIdempotent() {
        Registration r = makeRegistration();
        r.reserve();
        r.cancel();
        // tweede cancel mag geen exception gooien
        assertThatNoException().isThrownBy(r::cancel);
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
    }

    @Test
    void confirm_vanafREQUESTED_gooit_exception() {
        Registration r = makeRegistration();
        assertThatThrownBy(r::confirm)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markNoShow_vanafCONFIRMED_zettStatusOpNO_SHOW() {
        Registration r = makeRegistration();
        r.reserve();
        r.confirm();
        r.markNoShow();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.NO_SHOW);
    }

    @Test
    void reserve_vanafWAITLISTED_zettStatusOpRESERVED() {
        Registration r = makeRegistration();
        r.waitlist();
        r.reserve();
        assertThat(r.getStatus()).isEqualTo(RegistrationStatus.RESERVED);
    }

    @Test
    void idempotencyKey_wordtOpgeslagen() {
        String key = UUID.randomUUID().toString();
        Registration r = Registration.create(
                UUID.randomUUID(), UUID.randomUUID(), 0, null, "WEB", key);
        assertThat(r.getIdempotencyKey()).isEqualTo(key);
    }
}

