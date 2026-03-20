Feature: Schema bewerken (SCH-05)
  Als OWNER wil ik het schema kunnen bewerken
  zodat ik last-minute wijzigingen kan doorvoeren.

  Scenario: Sessie succesvol bijwerken
    Given ik ben ingelogd als OWNER van event 5
    And sessie "Keynote" heeft tijdslot 09:00 tot 10:00
    When ik een PUT-verzoek stuur naar /events/5/schedule/sessions/1 met een nieuw tijdslot 10:00 tot 11:00
    Then wordt het tijdslot bijgewerkt
    And ontvangen aangemelde deelnemers een notificatie over de wijziging

  Scenario: Sessie verwijderen uit schema
    Given ik ben ingelogd als OWNER van event 5
    When ik een DELETE-verzoek stuur naar /events/5/schedule/sessions/1
    Then wordt de sessie verwijderd uit het schema
