Feature: Bericht verwijderen (MSG-04)
  Als OWNER of ADM wil ik ongepaste berichten kunnen verwijderen
  zodat de communicatie binnen het platform veilig en respectvol blijft.

  Scenario: Bericht verwijderen als ADM
    Given ik ben ingelogd als ADM
    And er bestaat een bericht met id 12
    When ik een DELETE-verzoek stuur naar /messages/12
    Then wordt het bericht verwijderd en is niet meer zichtbaar voor beide partijen
    And wordt de verwijdering gelogd in het Audit-domein

  Scenario: Eigen bericht verwijderen als USER
    Given ik ben ingelogd als USER en heb bericht 12 verstuurd
    When ik een DELETE-verzoek stuur naar /messages/12
    Then wordt het bericht verwijderd

  Scenario: Bericht van iemand anders verwijderen als USER
    Given ik ben ingelogd als USER en bericht 12 is van een andere gebruiker
    When ik een DELETE-verzoek stuur naar /messages/12
    Then ontvang ik een 403 Forbidden foutmelding
