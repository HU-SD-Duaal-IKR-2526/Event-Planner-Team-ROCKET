Feature: Deelnemerslijst opvragen (REG-04)
  Als OWNER wil ik de deelnemerslijst van mijn event kunnen opvragen
  zodat ik de aanwezen kunnen beheren.

  Scenario: Lijst opvragen als OWNER
    Given ik ben ingelogd als OWNER van event 5
    When ik een GET-verzoek stuur naar /events/5/registrations
    Then ontvang ik een volledige lijst van aangemelde gebruikers

  Scenario: Lijst opvragen als gewone USER
    Given ik ben ingelogd als USER en geen OWNER van event 5
    When ik een GET-verzoek stuur naar /events/5/registrations
    Then ontvang ik een 403 Forbidden foutmelding
