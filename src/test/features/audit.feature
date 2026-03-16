Feature: Systeemacties loggen (AUD-01)
  Als systeem wil ik alle CRUD-acties op events en gebruikers automatisch loggen
  zodat er een volledige audittrail is.

  Scenario: Event aanmaken wordt gelogd
    Given een OWNER maakt een nieuw event aan
    When de POST /events request succesvol is verwerkt
    Then wordt een logentry aangemaakt met tijdstip, actor "OWNER" en actie "EVENT_CREATED"

  Scenario: Gebruiker verwijderen wordt gelogd
    Given een ADM verwijdert gebruiker met id 15
    When de DELETE /users/15 request succesvol is verwerkt
    Then wordt een logentry aangemaakt met tijdstip, actor "ADM" en actie "USER_DELETED"

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

Feature: Security-events bekijken (AUD-03)
  Als ADM wil ik security-events kunnen bekijken
  zodat ik potentiële aanvallen vroegtijdig kan detecteren.

  Scenario: Brute-force detectie
    Given gebruiker "hacker" heeft 5 mislukte inlogpogingen in de afgelopen 10 minuten
    When een 6e mislukte inlogpoging wordt geregistreerd
    Then wordt een security-alert gegenereerd
    And ontvangt de ADM een notificatie

  Scenario: Security-events gefilterd opvragen
    Given ik ben ingelogd als ADM
    When ik een GET-verzoek stuur naar /audit/security
    Then ontvang ik uitsluitend security-events apart gecategoriseerd van reguliere audit-entries

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
