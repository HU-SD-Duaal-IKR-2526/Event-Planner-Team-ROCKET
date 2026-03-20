Feature: Foutmeldingen inzien (AUD-04)
  Als ADM wil ik foutmeldingen en systeemfouten kunnen inzien
  zodat ik technische problemen snel kan opsporen en oplossen.

  Scenario: Kritieke fout gegenereerd
    Given een service gooit een onverwachte exception tijdens verwerking
    When de fout wordt vastgelegd
    Then bevat de logentry een stack trace, tijdstip en de naam van de betrokken service
    And ontvangt de ADM een directe notificatie

  Scenario: Foutlog filteren op service
    Given ik ben ingelogd als ADM
    When ik een GET-verzoek stuur naar /audit/errors?service=event-service
    Then ontvang ik een lijst van foutmeldingen afkomstig van de event-service
