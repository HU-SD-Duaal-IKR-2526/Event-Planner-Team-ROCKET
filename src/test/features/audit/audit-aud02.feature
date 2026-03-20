Feature: Audit-log inzien (AUD-02)
  Als ADM wil ik het audit-log kunnen inzien en filteren op datum, actor en domein
  zodat ik snel inzicht krijg in systeemactiviteit.

  Scenario: Audit-log filteren op datum en domein
    Given ik ben ingelogd als ADM
    When ik een GET-verzoek stuur naar /audit?domain=event&date=2026-06-01
    Then ontvang ik een gefilterde lijst van audit-entries voor het event-domein op die datum

  Scenario: Audit-log opvragen als gewone USER
    Given ik ben ingelogd als USER
    When ik een GET-verzoek stuur naar /audit
    Then ontvang ik een 403 Forbidden foutmelding
