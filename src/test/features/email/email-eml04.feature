Feature: Mailinglijst beheren (EML-04)
  Als OWNER of ADM wil ik mailinglijsten kunnen beheren
  zodat ik gericht kan communiceren met mijn doelgroep.

  Scenario: Mailinglijst aanmaken
    Given ik ben ingelogd als OWNER
    When ik een POST-verzoek stuur naar /mailing-lists met naam "VIP-gasten" en een lijst van adressen
    Then wordt de mailinglijst opgeslagen en gekoppeld aan mijn account

  Scenario: Mailinglijst exporteren als CSV
    Given ik ben ingelogd als OWNER en heb een mailinglijst "VIP-gasten"
    When ik een GET-verzoek stuur naar /mailing-lists/1/export
    Then ontvang ik een CSV-bestand met alle e-mailadressen van die lijst
