Feature: Capaciteit controleren (VEN-04)
  Als systeem wil ik de capaciteit van een locatie valideren bij elke nieuwe RSVP
  zodat het maximale aantal deelnemers niet wordt overschreden.

  Scenario: Aanmelding binnen capaciteit
    Given locatie van event 5 heeft capaciteit 100 en er zijn 99 aanmeldingen
    When een nieuwe gebruiker zich aanmeldt voor event 5
    Then wordt de aanmelding geaccepteerd

  Scenario: Aanmelding bij volledige capaciteit
    Given locatie van event 5 heeft capaciteit 100 en er zijn 100 aanmeldingen
    When een nieuwe gebruiker zich probeert aan te melden voor event 5
    Then wordt de aanmelding geweigerd met de melding "Capaciteit bereikt"
