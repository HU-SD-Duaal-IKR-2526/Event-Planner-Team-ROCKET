Feature: Event verwijderen (EVT-06)
  Als ADM wil ik een event kunnen verwijderen
  zodat verouderde of ongepaste events worden opgeruimd.

  Scenario: Event succesvol verwijderen
    Given ik ben ingelogd als ADM
    And er bestaat een event met id 7
    When ik een DELETE-verzoek stuur naar /events/7
    Then wordt het event inclusief alle gekoppelde registraties verwijderd
    And ontvang ik een 204 No Content response

  Scenario: Niet-bestaand event verwijderen
    Given ik ben ingelogd als ADM
    When ik een DELETE-verzoek stuur naar /events/9999
    Then ontvang ik een 404 Not Found foutmelding
