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
