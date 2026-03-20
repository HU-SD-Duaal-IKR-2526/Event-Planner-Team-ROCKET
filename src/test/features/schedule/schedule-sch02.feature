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
