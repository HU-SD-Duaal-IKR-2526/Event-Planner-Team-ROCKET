Feature: Event bekijken (EVT-03)
  Als gebruiker wil ik alle beschikbare events kunnen bekijken
  zodat ik kan beslissen voor welk event ik mij wil aanmelden.

  Scenario: Lijst van geplande events opvragen
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /events
    Then ontvang ik een lijst van alle events met status PLANNED

  Scenario: Lege lijst wanneer er geen events zijn
    Given er zijn geen events aangemaakt
    When ik een GET-verzoek stuur naar /events
    Then ontvang ik een lege lijst met statuscode 200
