Feature: Herinnering sturen (EML-03)
  Als systeem wil ik automatisch een herinnering sturen 24 uur voor een event
  zodat deelnemers niet vergeten aanwezig te zijn.

  Scenario: Herinnering verstuurd 24 uur voor aanvang
    Given event "TechTalk" vindt morgen plaats
    And "jan" is aangemeld voor dit event
    When de scheduler de herinneringstaak uitvoert
    Then ontvangt "jan" een herinneringsmail
    And wordt de verzending gelogd om duplicaten te voorkomen

  Scenario: Herinnering niet nogmaals versturen
    Given de herinnering voor event "TechTalk" is al verstuurd aan "jan"
    When de scheduler de herinneringstaak opnieuw uitvoert
    Then wordt er geen tweede herinnering verstuurd aan "jan"
