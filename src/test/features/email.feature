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

Feature: Bevestigingsmail sturen (EML-02)
  Als systeem wil ik automatisch een bevestigingsmail sturen bij succesvolle RSVP
  zodat de gebruiker een bewijs van aanmelding heeft.

  Scenario: Bevestigingsmail na aanmelding
    Given gebruiker "jan" heeft zich succesvol aangemeld voor event "TechTalk"
    When de registratie wordt bevestigd
    Then ontvangt "jan" een e-mail met de event-naam, datum, locatie en een unieke registratiecode

  Scenario: Bevestigingsmail mislukt door ongeldig adres
    Given gebruiker "jan" heeft geen geldig e-mailadres opgegeven
    When het systeem de bevestigingsmail probeert te sturen
    Then wordt de mislukte verzending gelogd
    And wordt de registratie toch als geslaagd gemarkeerd

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
