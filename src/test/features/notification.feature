Feature: Notificatie ontvangen (NOT-01)
  Als gebruiker wil ik een notificatie ontvangen wanneer een event wordt gewijzigd of geannuleerd
  zodat ik tijdig geïnformeerd ben.

  Scenario: Notificatie bij event-annulering
    Given ik ben aangemeld voor event 5
    And een OWNER annuleert event 5
    When de annulering wordt verwerkt
    Then ontvang ik een notificatie met de melding dat event 5 is geannuleerd

  Scenario: Notificatie bij wijziging event-datum
    Given ik ben aangemeld voor event 5
    When de OWNER de datum van event 5 wijzigt
    Then ontvang ik een notificatie met de nieuwe datum

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

Feature: Notificatie markeren als gelezen (NOT-03)
  Als gebruiker wil ik notificaties kunnen markeren als gelezen
  zodat mijn berichtencentrum overzichtelijk blijft.

  Scenario: Notificatie succesvol markeren als gelezen
    Given ik heb een ongelezen notificatie met id 9
    When ik een PUT-verzoek stuur naar /notifications/9/read
    Then krijgt de notificatie de status "gelezen"
    And wordt de teller van ongelezen notificaties verlaagd

  Scenario: Alle notificaties in één keer markeren als gelezen
    Given ik heb 5 ongelezen notificaties
    When ik een PUT-verzoek stuur naar /notifications/read-all
    Then worden alle notificaties als gelezen gemarkeerd
    And is de teller van ongelezen notificaties 0

Feature: Pushnotificatie versturen (NOT-04)
  Als systeem wil ik pushnotificaties versturen bij relevante events
  zodat gebruikers proactief worden geïnformeerd.

  Scenario: Pushnotificatie bij event morgen
    Given gebruiker "jan" is aangemeld voor event 5 dat morgen plaatsvindt
    And "jan" heeft pushnotificaties ingeschakeld
    When de dagelijkse reminder-scheduler wordt uitgevoerd
    Then ontvangt "jan" een pushnotificatie met een herinnering aan event 5

  Scenario: Geen pushnotificatie bij uitgeschakelde instelling
    Given gebruiker "jan" heeft pushnotificaties uitgeschakeld
    When de scheduler een pushnotificatie probeert te sturen
    Then wordt er geen pushnotificatie verstuurd naar "jan"
