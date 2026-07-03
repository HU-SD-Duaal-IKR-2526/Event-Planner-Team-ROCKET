package nl.teamrocket.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Core Modulaire Monoliet — Event module entrypoint.
 *
 * Architectuurdoc §3.1.1: in de doelarchitectuur delen userprofile, event,
 * registration, venue en schedule één Spring Boot deployable. In deze
 * fase van het project wordt elke module in een eigen repo ontwikkeld
 * (zie user_profile repo van Joshua); de package-structuur is alvast op
 * de monoliet uitgelijnd zodat samenvoegen later triviaal is.
 *
 * Deze deployable bevat uitsluitend de event-module
 * ({@code nl.teamrocket.core.event}). Hexagonale lagen: domain → application → adapter.
 */
@SpringBootApplication
public class CoreMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreMonolithApplication.class, args);
    }
}
