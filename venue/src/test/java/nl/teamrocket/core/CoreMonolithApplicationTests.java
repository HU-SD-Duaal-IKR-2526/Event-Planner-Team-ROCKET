package nl.teamrocket.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: Spring context laden op H2 (dev profile). Dekt af dat alle beans
 * gewired worden: hexagonale layering, JPA-mapping, REST controllers, exception
 * handler, en de stub ACL gateway (StubExternalVenueGateway @PostConstruct seed).
 */
@SpringBootTest
class CoreMonolithApplicationTests {

    @Test
    void contextLoads() {
        // Spring fails de test als de context niet laadt
    }
}
