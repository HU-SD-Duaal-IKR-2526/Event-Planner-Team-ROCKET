Feature: Gebruiker verwijderen (USR-06)
  Als ADM wil ik een gebruiker kunnen verwijderen
  zodat inactieve of problematische accounts worden opgeruimd.

  Scenario: Gebruiker succesvol verwijderen
    Given ik ben ingelogd als ADM
    And er bestaat een gebruiker met id 15
    When ik een DELETE-verzoek stuur naar /users/15
    Then wordt de gebruiker verwijderd
    And ontvang ik een 204 No Content response

  Scenario: Niet-bestaande gebruiker verwijderen
    Given ik ben ingelogd als ADM
    When ik een DELETE-verzoek stuur naar /users/9999
    Then ontvang ik een 404 Not Found foutmelding
