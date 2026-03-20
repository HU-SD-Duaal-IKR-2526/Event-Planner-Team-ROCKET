Feature: Profiel bekijken (USR-01)
  Als gebruiker wil ik mijn eigen profiel kunnen bekijken
  zodat ik mijn geregistreerde gegevens kan inzien.

  Scenario: Eigen profiel opvragen
    Given ik ben ingelogd als gebruiker met id 42
    When ik een GET-verzoek stuur naar /users/42
    Then ontvang ik mijn profielgegevens inclusief gebruikersnaam en rol
    And staat het wachtwoord niet in de response

  Scenario: Profiel van andere gebruiker opvragen zonder rechten
    Given ik ben ingelogd als gebruiker met id 42
    When ik een GET-verzoek stuur naar /users/99
    Then ontvang ik een 403 Forbidden foutmelding
