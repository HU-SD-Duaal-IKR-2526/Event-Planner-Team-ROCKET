Feature: Notificaties bekijken (NOT-02)
  Als gebruiker wil ik al mijn notificaties kunnen bekijken
  zodat ik geen meldingen mis.

  Scenario: Notificaties ophalen
    Given ik ben ingelogd als USER en heb 3 ongelezen notificaties
    When ik een GET-verzoek stuur naar /notifications
    Then ontvang ik een lijst van alle notificaties gesorteerd op datum
    And zijn ongelezen notificaties duidelijk gemarkeerd

  Scenario: Geen notificaties aanwezig
    Given ik heb nog geen notificaties ontvangen
    When ik een GET-verzoek stuur naar /notifications
    Then ontvang ik een lege lijst met statuscode 200
