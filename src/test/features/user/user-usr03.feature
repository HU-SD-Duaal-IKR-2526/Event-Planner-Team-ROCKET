Feature: Profielfoto uploaden (USR-03)
  Als gebruiker wil ik een profielfoto kunnen uploaden
  zodat andere gebruikers mij kunnen herkennen.

  Scenario: Geldige foto uploaden
    Given ik ben ingelogd als gebruiker
    When ik een POST-verzoek stuur naar /users/42/avatar met een PNG-bestand van 2 MB
    Then wordt de foto opgeslagen en gekoppeld aan mijn profiel

  Scenario: Bestand te groot
    Given ik ben ingelogd als gebruiker
    When ik een foto probeer te uploaden van 10 MB
    Then ontvang ik een 413 Payload Too Large foutmelding

  Scenario: Ongeldig bestandsformaat
    Given ik ben ingelogd als gebruiker
    When ik een PDF-bestand probeer te uploaden als profielfoto
    Then ontvang ik een 415 Unsupported Media Type foutmelding
