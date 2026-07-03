package nl.teamrocket.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Core Modulaire Monoliet — Venue module entrypoint.
 *
 * Architectuurdoc §3.1.1: in de doelarchitectuur delen alle Core bounded contexts
 * één Spring Boot deployable. In deze fase wordt elke module in een eigen repo
 * ontwikkeld; de package-structuur ({@code nl.teamrocket.core.<module>}) is alvast
 * op samenvoeging uitgelijnd.
 *
 * Deze deployable bevat uitsluitend de venue-module
 * ({@code nl.teamrocket.core.venue}). Hexagonale lagen: domain → application → adapter.
 */
@SpringBootApplication
public class CoreMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreMonolithApplication.class, args);
    }
}
