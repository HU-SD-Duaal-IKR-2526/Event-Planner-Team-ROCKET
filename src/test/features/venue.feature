Feature: Locatie toevoegen (VEN-01)
  Als ADM wil ik een nieuwe locatie kunnen toevoegen
  zodat OWNER's deze kunnen selecteren voor hun events.

  Scenario: Locatie succesvol toevoegen
    Given ik ben ingelogd als ADM
    When ik een POST-verzoek stuur naar /venues met naam "Beurs van Berlage", adres en capaciteit 500
    Then wordt de locatie opgeslagen en beschikbaar voor koppeling aan events

  Scenario: Locatie toevoegen zonder capaciteit
    Given ik ben ingelogd als ADM
    When ik een POST-verzoek stuur naar /venues zonder capaciteit
    Then ontvang ik een 400 Bad Request foutmelding

Feature: Locatie bekijken (VEN-02)
  Als gebruiker wil ik de locatiedetails van een event kunnen bekijken
  zodat ik weet waar ik naartoe moet.

  Scenario: Locatie van event opvragen
    Given er bestaat een event 5 gekoppeld aan locatie "Beurs van Berlage"
    When ik een GET-verzoek stuur naar /events/5/venue
    Then ontvang ik de locatiegegevens inclusief naam, adres en capaciteit

  Scenario: Event zonder gekoppelde locatie
    Given event 5 heeft geen gekoppelde locatie
    When ik een GET-verzoek stuur naar /events/5/venue
    Then ontvang ik een 404 Not Found foutmelding

Feature: Locatie koppelen aan event (VEN-03)
  Als OWNER wil ik een bestaande locatie kunnen koppelen aan mijn event
  zodat de evenementlocatie correct wordt weergegeven.

  Scenario: Locatie succesvol koppelen
    Given ik ben ingelogd als OWNER van event 5
    And locatie "Beurs van Berlage" heeft capaciteit 500 en er zijn 150 aanmeldingen
    When ik een PUT-verzoek stuur naar /events/5 met locatie-id 3
    Then wordt de locatie gekoppeld aan event 5

  Scenario: Locatie koppelen met onvoldoende capaciteit
    Given locatie "Kleine Zaal" heeft een capaciteit van 10
    And event 5 heeft al 10 aanmeldingen
    When ik locatie "Kleine Zaal" probeer te koppelen aan event 5
    Then ontvang ik een 409 Conflict foutmelding met de melding "Capaciteit onvoldoende"

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

Feature: Locatie verwijderen (VEN-05)
  Als ADM wil ik een locatie kunnen verwijderen
  zodat verouderde locaties worden opgeruimd.

  Scenario: Locatie succesvol verwijderen
    Given ik ben ingelogd als ADM
    And locatie 3 heeft geen actieve (PLANNED) events gekoppeld
    When ik een DELETE-verzoek stuur naar /venues/3
    Then wordt de locatie verwijderd
    And ontvang ik een 204 No Content response

  Scenario: Locatie verwijderen met actief event
    Given locatie 3 is gekoppeld aan event 5 met status PLANNED
    When ik een DELETE-verzoek stuur naar /venues/3
    Then ontvang ik een 409 Conflict foutmelding met de melding "Locatie heeft nog actieve events"
