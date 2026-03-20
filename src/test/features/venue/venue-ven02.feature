Feature: Locatie bekijken (VEN-02)
  Als gebruiker wil ik de locatiedetails van een event kunnen bekijken
  zodat ik weet waar ik naartoe moet.

  Scenario: Locatie van event opvragen
    Given er bestaat een event 5 gekoppeld aan locatie "Beurs van Berlage"
    When ik een GET-verzoek stuur naar /events/5/venue
    Then ontvang ik de locatiegegevens inclusief naam, adres en capaciteit

  Scenario: Event zonder gekoppelde locatie
    Given event 5 heeft geen gekoppelde locatie
    When ik een GET-verzoek stuur naar /events/5/venue
    Then ontvang ik een 404 Not Found foutmelding
