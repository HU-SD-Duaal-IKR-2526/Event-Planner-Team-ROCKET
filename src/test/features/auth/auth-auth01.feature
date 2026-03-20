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
