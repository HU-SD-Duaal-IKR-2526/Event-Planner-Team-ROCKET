package nl.teamrocket.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: laad de Spring context (H2 in dev profile). Dekt af dat alle beans
 * gewired worden — hexagonale layering, JPA-mapping, REST controllers, exception
 * handler. Vangt configuratiefouten vóór deployment.
 */
@SpringBootTest
class CoreMonolithApplicationTests {

    @Test
    void contextLoads() {
        // intentionally empty — Spring fails the test if the context cannot load
    }
}
