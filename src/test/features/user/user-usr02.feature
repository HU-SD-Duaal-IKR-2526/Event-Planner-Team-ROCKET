Feature: Profiel bewerken (USR-02)
  Als gebruiker wil ik mijn profielgegevens kunnen bewerken
  zodat mijn profiel up-to-date blijft.

  Scenario: Naam succesvol bijwerken
    Given ik ben ingelogd als gebruiker met id 42
    When ik een PUT-verzoek stuur naar /users/42 met een nieuwe naam
    Then worden de bijgewerkte gegevens opgeslagen
    And geeft de response de nieuwe profielinformatie terug

  Scenario: Ander profiel bewerken zonder rechten
    Given ik ben ingelogd als gebruiker
    When ik een PUT-verzoek stuur naar /users/99
    Then ontvang ik een 403 Forbidden foutmelding
