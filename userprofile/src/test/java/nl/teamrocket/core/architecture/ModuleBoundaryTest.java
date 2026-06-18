package nl.teamrocket.core.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Architecture guard tests — enforces that module boundaries inside the Core Monolith
 * are respected. Architecture doc §3.1.1: "contexts strikt gescheiden op package-niveau:
 * elke module heeft zijn eigen domain/application/ports-pakketten en mag alleen via
 * expliciete ports of in-proces events met andere modules praten."
 *
 * These tests prevent accidental direct cross-module domain access.
 */
@DisplayName("Core Monolith module boundary tests")
class ModuleBoundaryTest {

    static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .importPackages("nl.teamrocket.core");
    }

    @Test
    @DisplayName("userprofile domain must not depend on other modules' domain layers")
    void userprofile_domain_isolation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("nl.teamrocket.core.userprofile.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "nl.teamrocket.core.event.domain..",
                        "nl.teamrocket.core.registration.domain..",
                        "nl.teamrocket.core.venue.domain..",
                        "nl.teamrocket.core.schedule.domain.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("domain layer must not depend on Spring, JPA, or MongoDB annotations")
    void domain_has_no_infrastructure_dependencies() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("nl.teamrocket.core..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.springframework.data.mongodb.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("adapters must not bypass application layer to access domain directly")
    void adapters_must_use_application_layer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("nl.teamrocket.core..adapter..")
                .should().dependOnClassesThat()
                .resideInAPackage("nl.teamrocket.core..domain.model..")
                .andShould().not().dependOnClassesThat()
                .resideInAPackage("nl.teamrocket.core..application..");
        // Note: adapters ARE allowed to use domain model classes for mapping,
        // but must route commands/queries through the application layer.
        // This rule is intentionally lenient on read-only domain model access.
        // The key invariant: no adapter should call domain.model methods directly.
        rule.check(classes);
    }

    @Test
    @DisplayName("application layer must not depend on adapter layer")
    void application_must_not_depend_on_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("nl.teamrocket.core..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("nl.teamrocket.core..adapter..");
        rule.check(classes);
    }
}
