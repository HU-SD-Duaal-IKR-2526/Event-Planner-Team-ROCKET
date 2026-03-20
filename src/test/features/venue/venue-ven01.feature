Feature: Locatie toevoegen (VEN-01)
  Als ADM wil ik een nieuwe locatie kunnen toevoegen
  zodat OWNERS deze kunnen selecteren voor hun events.

  Scenario: Locatie succesvol toevoegen
    Given ik ben ingelogd als ADM
    When ik een POST-verzoek stuur naar /venues met naam "Beurs van Berlage", adres en capaciteit 500
    Then wordt de locatie opgeslagen en beschikbaar voor koppeling aan events

  Scenario: Locatie toevoegen zonder capaciteit
    Given ik ben ingelogd als ADM
    When ik een POST-verzoek stuur naar /venues zonder capaciteit
    Then ontvang ik een 400 Bad Request foutmelding
