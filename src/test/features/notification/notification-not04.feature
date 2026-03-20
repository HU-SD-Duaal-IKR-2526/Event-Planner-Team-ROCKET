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
