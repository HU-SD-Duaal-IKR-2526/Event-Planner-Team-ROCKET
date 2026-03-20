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
