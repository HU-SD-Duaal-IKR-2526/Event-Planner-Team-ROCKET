Feature: Event annuleren (EVT-05)
  Als OWNER wil ik een event kunnen annuleren
  zodat deelnemers tijdig worden geïnformeerd.

  Scenario: Event succesvol annuleren
    Given ik ben ingelogd als OWNER van event 7
    And event 7 heeft status PLANNED
    When ik een PUT-verzoek stuur naar /events/cancel/7
    Then krijgt het event status CANCELLED
    And ontvangen alle aangemelde deelnemers een notificatie

  Scenario: Reeds geannuleerd event annuleren
    Given event 7 heeft al status CANCELLED
    When ik een PUT-verzoek stuur naar /events/cancel/7
    Then ontvang ik een 409 Conflict foutmelding
