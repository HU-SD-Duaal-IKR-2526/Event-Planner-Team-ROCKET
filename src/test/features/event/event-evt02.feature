Feature: Event bewerken (EVT-02)
  Als OWNER wil ik een bestaand event kunnen bewerken
  zodat ik wijzigingen in datum, locatie of beschrijving kan doorvoeren.

  Scenario: Event succesvol bewerken
    Given ik ben ingelogd als OWNER van event 7
    When ik een PUT-verzoek stuur naar /events/7 met een nieuwe locatie "Amsterdam"
    Then worden de wijzigingen opgeslagen
    And geeft de response de bijgewerkte event-details terug

  Scenario: Event bewerken van een ander OWNER
    Given ik ben ingelogd als OWNER maar niet de eigenaar van event 7
    When ik een PUT-verzoek stuur naar /events/7
    Then ontvang ik een 403 Forbidden foutmelding
