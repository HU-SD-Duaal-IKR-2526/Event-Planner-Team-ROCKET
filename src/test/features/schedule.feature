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

Feature: Sessie toevoegen aan schema (SCH-02)
  Als OWNER wil ik sessies kunnen toevoegen aan het event-schema
  zodat het programma volledig is uitgewerkt.

  Scenario: Sessie succesvol toevoegen
    Given ik ben ingelogd als OWNER van event 5
    And event 5 heeft een dagschema
    When ik een POST-verzoek stuur naar /events/5/schedule/sessions met titel "Keynote", start "09:00" en eind "10:00"
    Then wordt de sessie toegevoegd aan het schema

  Scenario: Overlappende tijdslots
    Given event 5 heeft al een sessie van 09:00 tot 10:00
    When ik een sessie probeer toe te voegen van 09:30 tot 10:30
    Then ontvang ik een 409 Conflict foutmelding met de melding "Tijdslot overlapt met bestaande sessie"

Feature: Tijdslot bekijken (SCH-03)
  Als gebruiker wil ik het programma van een event kunnen bekijken
  zodat ik weet welke sessies er zijn en wanneer ze plaatsvinden.

  Scenario: Programma opvragen
    Given ik ben ingelogd als USER en aangemeld voor event 5
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then ontvang ik een chronologisch overzicht van alle sessies

  Scenario: Programma opvragen van event zonder schema
    Given event 5 heeft geen dagschema
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then ontvang ik een 404 Not Found foutmelding

Feature: Parallelle activiteiten inzien (SCH-04)
  Als gebruiker wil ik parallelle activiteiten kunnen inzien
  zodat ik een weloverwogen keuze kan maken welke sessie ik bijwoon.

  Scenario: Parallelle sessies opvragen
    Given event 5 heeft twee sessies tegelijk van 10:00 tot 11:00
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then worden de twee parallelle sessies naast elkaar weergegeven in de tijdlijn

  Scenario: Geen parallelle sessies aanwezig
    Given event 5 heeft alleen opeenvolgende sessies
    When ik het schema opvraag
    Then worden alle sessies chronologisch weergegeven zonder parallelle weergave

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
