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
