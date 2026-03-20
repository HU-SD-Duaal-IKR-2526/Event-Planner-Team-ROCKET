Feature: Inloggen (AUTH-02)
  Als gebruiker wil ik kunnen inloggen met mijn credentials
  zodat ik een JWT-token ontvang waarmee ik beveiligde endpoints kan benaderen.

  Scenario: Succesvol inloggen
    Given ik ben een geregistreerde gebruiker met gebruikersnaam "jan" en wachtwoord "Geheim1!"
    When ik een POST-verzoek stuur naar /auth/login met de juiste credentials
    Then ontvang ik een geldig JWT-token in de response

  Scenario: Inloggen met verkeerd wachtwoord
    Given ik ben een geregistreerde gebruiker
    When ik inlog met een onjuist wachtwoord
    Then ontvang ik een 401 Unauthorized foutmelding
    And wordt de mislukte inlogpoging gelogd
