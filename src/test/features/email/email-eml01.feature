Feature: Uitnodiging versturen (EML-01)
  Als OWNER wil ik uitnodigingen kunnen versturen naar specifieke gebruikers of mailinglijsten
  zodat ik mijn event kan promoten.

  Scenario: Uitnodiging succesvol versturen
    Given ik ben ingelogd als OWNER van event 5
    When ik een POST-verzoek stuur naar /email/invite met een lijst van e-mailadressen
    Then wordt de uitnodigingsmail verstuurd naar alle opgegeven adressen
    And wordt de verzending gelogd

  Scenario: Uitnodiging naar ongeldige e-mailadressen
    Given ik verstuur een uitnodiging naar "geen-geldig-adres"
    When de e-mail wordt verwerkt
    Then wordt het ongeldige adres overgeslagen
    And ontvang ik een waarschuwing in de response
