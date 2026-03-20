Feature: Dagschema aanmaken (SCH-01)
  Als OWNER wil ik een dagschema aanmaken voor mijn event
  zodat deelnemers weten wat de planning van de dag is.

  Scenario: Schema succesvol aanmaken
    Given ik ben ingelogd als OWNER van event 5
    When ik een POST-verzoek stuur naar /events/5/schedule met startdatum "2026-06-01"
    Then wordt het dagschema aangemaakt en gekoppeld aan event 5

  Scenario: Schema aanmaken voor event dat al een schema heeft
    Given event 5 heeft al een dagschema
    When ik opnieuw een POST-verzoek stuur naar /events/5/schedule
    Then ontvang ik een 409 Conflict foutmelding
