package nl.teamrocket.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Core Modulaire Monoliet entry point.
 *
 * Architecture doc §3.1.1 — the following bounded contexts share this single deployable:
 *   - userprofile  (this module, Joshua)
 *   - event        (Wessel — to be added)
 *   - registration (Johan — to be added)
 *   - venue        (Wessel — to be added)
 *   - schedule     (Johan — to be added)
 *
 * Each module has its own package with domain/application/ports/adapters layers.
 * Cross-module communication happens via in-process domain events or explicit ports.
 * ArchUnit tests (ModuleBoundaryTest) enforce that modules do not directly access
 * each other's domain layers.
 */
@SpringBootApplication
@EnableScheduling
public class CoreMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreMonolithApplication.class, args);
    }
}
