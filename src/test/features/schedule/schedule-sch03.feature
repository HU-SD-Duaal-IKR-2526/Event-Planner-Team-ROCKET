Feature: Tijdslot bekijken (SCH-03)
  Als gebruiker wil ik het programma van een event kunnen bekijken
  zodat ik weet welke sessies er zijn en wanneer ze plaatsvinden.

  Scenario: Programma opvragen
    Given ik ben ingelogd als USER en aangemeld voor event 5
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then ontvang ik een chronologisch overzicht van alle sessies

  Scenario: Programma opvragen van event zonder schema
    Given event 5 heeft geen dagschema
    When ik een GET-verzoek stuur naar /events/5/schedule
    Then ontvang ik een 404 Not Found foutmelding
