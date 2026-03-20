Feature: Afmelden voor event (REG-02)
  Als gebruiker wil ik mij kunnen afmelden voor een event
  zodat er ruimte vrijkomt voor andere deelnemers.

  Scenario: Succesvolle afmelding
    Given ik ben ingelogd als USER en aangemeld voor event 5
    When ik een DELETE-verzoek stuur naar /registrations/5
    Then wordt mijn aanmelding verwijderd
    And wordt de beschikbare capaciteit van het event verhoogd

  Scenario: Afmelden voor event waarbij ik niet aangemeld ben
    Given ik ben niet aangemeld voor event 5
    When ik een DELETE-verzoek stuur naar /registrations/5
    Then ontvang ik een 404 Not Found foutmelding
