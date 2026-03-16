Feature: Registreren (AUTH-01)
  Als nieuwe gebruiker wil ik mij kunnen registreren
  zodat ik toegang krijg tot de applicatie.

  Scenario: Succesvolle registratie
    Given ik ben een niet-geregistreerde gebruiker
    When ik een POST-verzoek stuur naar /auth/register met een unieke gebruikersnaam en een geldig wachtwoord
    Then wordt mijn account aangemaakt met rol USER
    And wordt het wachtwoord gehasht opgeslagen

  Scenario: Registratie met bestaande gebruikersnaam
    Given er bestaat al een gebruiker met gebruikersnaam "jan"
    When ik probeer te registreren met gebruikersnaam "jan"
    Then ontvang ik een 409 Conflict foutmelding
    And wordt er geen nieuw account aangemaakt

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

Feature: Wachtwoord resetten (AUTH-03)
  Als gebruiker wil ik mijn wachtwoord kunnen resetten via e-mail
  zodat ik weer toegang krijg als ik mijn wachtwoord ben vergeten.

  Scenario: Reset-link aanvragen
    Given ik ben een geregistreerde gebruiker met e-mailadres "jan@example.com"
    When ik een POST-verzoek stuur naar /auth/reset-password met mijn e-mailadres
    Then ontvang ik een e-mail met een reset-link
    And is de reset-link 1 uur geldig

  Scenario: Reset-link gebruiken na verlopen
    Given ik heb een reset-link ontvangen die meer dan 1 uur geleden is aangemaakt
    When ik de verlopen reset-link gebruik
    Then ontvang ik een 400 Bad Request foutmelding

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

Feature: JWT-token valideren (AUTH-05)
  Als systeem wil ik bij elke request het JWT-token valideren
  zodat alleen geauthenticeerde gebruikers toegang krijgen tot beveiligde endpoints.

  Scenario: Geldig token
    Given een gebruiker stuurt een request met een geldig JWT-token
    When het systeem het token valideert
    Then wordt de request doorgelaten op basis van de rol in het token

  Scenario: Verlopen token
    Given een gebruiker stuurt een request met een verlopen JWT-token
    When het systeem het token valideert
    Then wordt de request geweigerd met een 401 Unauthorized foutmelding
