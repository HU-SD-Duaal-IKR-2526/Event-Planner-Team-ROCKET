Feature: Uitloggen (AUTH-04)
  Als gebruiker wil ik kunnen uitloggen
  zodat mijn sessie veilig wordt beëindigd.

  Scenario: Succesvol uitloggen
    Given ik ben ingelogd en beschik over een geldig JWT-token
    When ik een POST-verzoek stuur naar /auth/logout
    Then wordt mijn token ongeldig gemaakt
    And ontvang ik een 200 OK response

  Scenario: Uitgelogd token hergebruiken
    Given ik heb mij uitgelogd
    When ik een beveiligd endpoint probeer te benaderen met het oude token
    Then ontvang ik een 401 Unauthorized foutmelding
