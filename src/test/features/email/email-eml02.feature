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
