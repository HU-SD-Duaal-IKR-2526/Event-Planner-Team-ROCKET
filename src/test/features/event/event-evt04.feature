Feature: Event detail opvragen (EVT-04)
  Als gebruiker wil ik de details van een specifiek event kunnen opvragen
  zodat ik volledig geïnformeerd ben.

  Scenario: Event detail succesvol opvragen
    Given er bestaat een event met id 5
    When ik een GET-verzoek stuur naar /events/5
    Then ontvang ik de volledige event-details inclusief naam, datum, locatie en beschrijving

  Scenario: Niet-bestaand event opvragen
    Given er bestaat geen event met id 9999
    When ik een GET-verzoek stuur naar /events/9999
    Then ontvang ik een 404 Not Found foutmelding
